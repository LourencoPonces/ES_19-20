# TODO: use variables
locals {
	dns_root = "quiztutor.breda.pt"
}

provider "google" {
	version = "~> 3.24"
	credentials = file("credentials.json")
	region = "europe-west1"
	project = "quizzestutor"
}

resource "google_storage_bucket" "tf_state" {
	name = "quizzestutor-tf-state"
	location = "europe-west1"
	storage_class = "REGIONAL"
	versioning {
		enabled = true
	}

	# delete old states
	lifecycle_rule {
		condition {
			num_newer_versions = 5
		}
		action {
			type = "Delete"
		}
	}

	bucket_policy_only = true

	lifecycle {
		prevent_destroy = true
	}
}

resource "google_storage_bucket_iam_binding" "tf_state_tf_svc" {
	bucket = google_storage_bucket.tf_state.name
	role = "roles/storage.objectAdmin"
	members = ["serviceAccount:terraform@quizzestutor.iam.gserviceaccount.com"]

	lifecycle {
		prevent_destroy = true
	}
}

terraform {
	backend "gcs" {
		bucket = "quizzestutor-tf-state"
		credentials = "credentials.json"
	}
}

resource "random_string" "suffix" {
	length = 8
	upper = false
	special = false
}

resource "google_project_iam_custom_role" "storageObjectsGetOnly" {
	role_id = "storageObjectsGetOnly_${random_string.suffix.result}"
	title = "Role that only grants permission to read bucket objects (storage.objects.get)"
	permissions = ["storage.objects.get"]
}

# DNS

resource "google_dns_managed_zone" "default" {
	name = "quizzestutor-dns-zone-${random_string.suffix.result}"
	dns_name = "${local.dns_root}."

	lifecycle {
		prevent_destroy = true # may require NS record updates after recreation
	}
}

resource "google_dns_record_set" "default" {
	for_each = {
		"IPV4" = "A"
		"IPV6" = "AAAA"
	}

	managed_zone = google_dns_managed_zone.default.name
	name = google_dns_managed_zone.default.dns_name
	type = each.value
	ttl = 300

	rrdatas = [google_compute_global_address.frontend_lbal[each.key].address]
}

resource "google_dns_record_set" "www" {
	for_each = {
		"IPV4" = "A"
		"IPV6" = "AAAA"
	}

	managed_zone = google_dns_managed_zone.default.name
	name = "www.${google_dns_managed_zone.default.dns_name}"
	type = each.value
	ttl = 300

	rrdatas = [google_compute_global_address.frontend_lbal[each.key].address]
}

resource "google_dns_record_set" "userassets" {
	for_each = {
		"IPV4" = "A"
		"IPV6" = "AAAA"
	}

	managed_zone = google_dns_managed_zone.default.name
	name = "userassets.${google_dns_managed_zone.default.dns_name}"
	type = each.value
	ttl = 300

	rrdatas = [google_compute_global_address.frontend_lbal[each.key].address]
}

# DB

resource "google_sql_database_instance" "default" {
	database_version = "POSTGRES_12"
	settings {
		tier  = "db-f1-micro"

		maintenance_window {
			day = 7
			hour = 2
		}
	}
}

resource "google_sql_database" "tutordb" {
	name = "tutordb"
	instance = google_sql_database_instance.default.name

	lifecycle {
		prevent_destroy = true
	}
}

resource "random_password" "tutordb" {
	length = 16
	special = true
}

resource "google_sql_user" "tutordb" {
	name = "tutordb"
	instance = google_sql_database_instance.default.id
	password = random_password.tutordb.result
}

# Backend

resource "google_storage_bucket" "userassets" {
	name = "quizzestutor-userassets-bucket-${random_string.suffix.result}"
	location = "europe-west1"
	storage_class = "REGIONAL"
	bucket_policy_only = true

	lifecycle {
		prevent_destroy = true
	}
}

resource "google_storage_bucket_iam_binding" "userassets_world" {
	bucket = google_storage_bucket.userassets.name
	members = ["allUsers"]
	role = google_project_iam_custom_role.storageObjectsGetOnly.id
}

resource "google_storage_bucket" "exports" {
	name = "quizzestutor-exports-${random_string.suffix.result}"
	location = "europe-west1"
	storage_class = "REGIONAL"
	bucket_policy_only = true

	lifecycle_rule {
		condition {
			age = "7"
		}
		action {
			type = "Delete"
		}
	}
}

resource "google_storage_bucket" "imports" {
	name = "quizzestutor-imports-${random_string.suffix.result}"
	location = "europe-west1"
	storage_class = "REGIONAL"
	bucket_policy_only = true

	lifecycle_rule {
		condition {
			age = "2"
		}
		action {
			type = "Delete"
		}
	}
}

# Frontend

resource "google_storage_bucket" "frontend" {
	name = "frontend-bucket-${random_string.suffix.result}"
	location = "europe-west1"
	storage_class = "REGIONAL"
	website {
		main_page_suffix = "index.html"

		# HACK: serve index.html for SPA routes
		not_found_page = "index.html"
	}

	bucket_policy_only = true
}

resource "google_storage_bucket_iam_binding" "frontend" {
	bucket = google_storage_bucket.frontend.name
	members = ["allUsers"]
	role = google_project_iam_custom_role.storageObjectsGetOnly.id
}

resource "google_compute_backend_bucket" "frontend" {
	name = "frontend-backend-bucket-${random_string.suffix.result}"
	bucket_name = google_storage_bucket.frontend.name
	enable_cdn = false
}

resource "google_compute_backend_bucket" "userassets" {
	name = "userassets-backend-bucket-${random_string.suffix.result}"
	bucket_name = google_storage_bucket.userassets.name
	enable_cdn = false
}

resource "google_compute_url_map" "frontend_lbal" {
	name = "frontend-lbal-url-map-${random_string.suffix.result}"

	host_rule {
		hosts = [local.dns_root, "www.${local.dns_root}"]
		path_matcher = "frontend"
	}

	path_matcher {
		name = "frontend"
		default_service = google_compute_backend_bucket.frontend.id
	}

	host_rule {
		hosts = ["userassets.${local.dns_root}"]
		path_matcher = "userassets"
	}

	path_matcher {
		name = "userassets"
		default_service = google_compute_backend_bucket.userassets.id
	}

	default_url_redirect {
		host_redirect = local.dns_root
		strip_query = false
		redirect_response_code = "FOUND"
	}
}

resource "google_compute_url_map" "frontend_lbal_https_redirect" {
	name = "frontend-lbal-url-map-https-redirect-${random_string.suffix.result}"

	default_url_redirect {
		https_redirect = true

		# this is the default, but terraform requires it anyway to ensure the block is not empty
		strip_query = false
	}
}

resource "google_compute_target_http_proxy" "frontend_lbal" {
	name = "frontend-lbal-http-${random_string.suffix.result}"
	url_map = google_compute_url_map.frontend_lbal_https_redirect.id
}

resource "google_compute_global_forwarding_rule" "frontend_lbal_http" {
	for_each = toset(["IPV4", "IPV6"])

	name = "frontend-forward-http-${lower(each.value)}-${random_string.suffix.result}"
	port_range = "80"
	ip_address = google_compute_global_address.frontend_lbal[each.value].address
	target = google_compute_target_http_proxy.frontend_lbal.id
}

resource "google_compute_target_https_proxy" "frontend_lbal" {
	name = "frontend-lbal-https-${random_string.suffix.result}"
	url_map = google_compute_url_map.frontend_lbal.id

	# TODO: create google managed certificate in terraform (currently in beta, possibly not a good idea, investigate)
	ssl_certificates = ["manual-quiztutor-frontend-cert-rbsyk4x1"]
}

resource "google_compute_global_forwarding_rule" "frontend_lbal_https" {
	for_each = toset(["IPV4", "IPV6"])

	name = "frontend-forward-https-${lower(each.value)}-${random_string.suffix.result}"
	port_range = "443"
	ip_address = google_compute_global_address.frontend_lbal[each.value].address
	target = google_compute_target_https_proxy.frontend_lbal.id
}

resource "google_compute_global_address" "frontend_lbal" {
	for_each = toset(["IPV4", "IPV6"])

	name = "frontend-lbal-addr-${lower(each.value)}-${random_string.suffix.result}"
	address_type = "EXTERNAL"
	ip_version = each.value
}
