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
}

resource "google_storage_bucket_iam_binding" "tf_state_tf_svc" {
	bucket = google_storage_bucket.tf_state.name
	role = "roles/storage.objectAdmin"
	members = ["serviceAccount:terraform@quizzestutor.iam.gserviceaccount.com"]
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

# DNS

resource "google_dns_managed_zone" "default" {
	name = "quiztutor-dns-zone-${random_string.suffix.result}"
	dns_name = "quiztutor.breda.pt."
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
	role = "roles/storage.objectViewer"
}

resource "google_compute_backend_bucket" "frontend" {
	name = "frontend-backend-bucket-${random_string.suffix.result}"
	bucket_name = google_storage_bucket.frontend.name
	enable_cdn = false
}

resource "google_compute_url_map" "frontend_lbal" {
	name = "frontend-lbal-url-map-${random_string.suffix.result}"
	default_service = google_compute_backend_bucket.frontend.id
}

resource "google_compute_target_http_proxy" "frontend_lbal" {
	name = "frontend-lbal-${random_string.suffix.result}"
	url_map = google_compute_url_map.frontend_lbal.id
}

resource "google_compute_global_forwarding_rule" "frontend_lbal_http" {
	for_each = toset(["IPV4", "IPV6"])

	name = "frontend-forward-http-${lower(each.value)}-${random_string.suffix.result}"
	port_range = "80"
	ip_address = google_compute_global_address.frontend_lbal[each.value].address
	target = google_compute_target_http_proxy.frontend_lbal.id
}

resource "google_compute_target_https_proxy" "frontend_lbal" {
	name = "frontend-lbal-${random_string.suffix.result}"
	url_map = google_compute_url_map.frontend_lbal.id

	# TODO: create google managed certificate in terraform (currently in beta, possibly not a good idea, investigate)
	ssl_certificates = ["manual-quiztutor-frontend-cert-rbsyk4x0"]
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
