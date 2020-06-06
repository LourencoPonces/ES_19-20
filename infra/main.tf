locals {
	dns_root = "quiztutor.breda.pt"
}

variable "git_commit_hash" {
	type = string
	description = "identifying hash for current git commit (output of git rev-parse HEAD)"
}

variable "fenix_oauth_id" {
	type = string
	description = "Fénix OAuth Client ID"
}

variable "fenix_oauth_secret" {
	type = string
	description = "Fénix OAuth Client Secret"
}

provider "google" {
	version = "~> 3.24"
	credentials = file("credentials.json")
	region = "europe-west1"
	project = "quizzestutor"
}

provider "random" {
	version = "~> 2.2"
}

provider "cloudinit" {
	version = "~> 1.0"
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

resource "google_dns_record_set" "backend" {
	for_each = {
		"IPV4" = "A"
		"IPV6" = "AAAA"
	}

	managed_zone = google_dns_managed_zone.default.name
	name = "backend.${google_dns_managed_zone.default.dns_name}"
	type = each.value
	ttl = 300

	rrdatas = [google_compute_global_address.frontend_lbal[each.key].address]
}

resource "google_compute_network" "backend" {
	name = "backend"
	auto_create_subnetworks = false
}

resource "google_compute_subnetwork" "backend_default" {
	name = "backend-default"
	network = google_compute_network.backend.id
	ip_cidr_range = "10.128.0.0/16"
	private_ip_google_access = true
}

resource "google_compute_firewall" "backend_allow_icmp_ssh_http" {
	name = "backend-allow-icmp-ssh-http"
	network = google_compute_network.backend.id

	allow {
		protocol = "icmp"
	}

	allow {
		protocol = "tcp"
		ports = ["80", "22"]
	}
}

data "google_compute_network" "backend" {
	name = google_compute_network.backend.name
}

data "google_compute_subnetwork" "backend_subnets" {
	for_each = toset(data.google_compute_network.backend.subnetworks_self_links)
	self_link = each.value
}

resource "google_compute_firewall" "backend_allow_internal" {
	name = "backend-allow-internal"
	network = google_compute_network.backend.id
	source_ranges = values(data.google_compute_subnetwork.backend_subnets)[*].ip_cidr_range

	allow {
		protocol = "udp"
	}

	allow {
		protocol = "tcp"
	}
}

# DB

resource "google_compute_global_address" "backend_svc_peering" {
	name = "backend-svc-peering-addr-${random_string.suffix.result}"
	purpose = "VPC_PEERING"
	address_type = "INTERNAL"
	prefix_length = 16
	network = google_compute_network.backend.id
}

resource "google_service_networking_connection" "backend" {
	network = google_compute_network.backend.id
	service = "servicenetworking.googleapis.com"
	reserved_peering_ranges = [google_compute_global_address.backend_svc_peering.name]
}

resource "google_sql_database_instance" "default" {
	database_version = "POSTGRES_12"
	settings {
		tier  = "db-f1-micro"
		ip_configuration {
			ipv4_enabled = false
			private_network = google_compute_network.backend.id
		}

		maintenance_window {
			day = 7
			hour = 2
		}
	}

	depends_on = [google_service_networking_connection.backend]
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

resource "google_service_account" "backend" {
	account_id = "backend-${random_string.suffix.result}"
}

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

resource "google_storage_bucket_iam_binding" "userassets_backend" {
	bucket = google_storage_bucket.userassets.name
	members = ["serviceAccount:${google_service_account.backend.email}"]
	role = "roles/storage.objectCreator"
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

resource "google_storage_bucket_iam_binding" "exports_backend" {
	bucket = google_storage_bucket.exports.name
	members = ["serviceAccount:${google_service_account.backend.email}"]
	role = "roles/storage.objectCreator"
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

resource "google_storage_bucket_iam_binding" "imports_backend" {
	bucket = google_storage_bucket.imports.name
	members = ["serviceAccount:${google_service_account.backend.email}"]
	role = "roles/storage.objectCreator"
}

data "google_compute_image" "backend" {
	family = "cos-81-lts"
	project = "gce-uefi-images"
}

resource "google_container_registry" "default" {
	location = "EU"
}

resource "google_storage_bucket_iam_member" "gcr_backend" {
	bucket = google_container_registry.default.id
	role = "roles/storage.objectViewer"
	member = "serviceAccount:${google_service_account.backend.email}"
}

data "google_container_registry_image" "backend" {
	name = "backend-gce"
	region = "eu"
	tag = var.git_commit_hash
}

resource "random_password" "auth_secret" {
	length = 100
	special = true
	override_special = "!@#%&*-_+?"

	# Invalidate sessions when underlying user data changes
	keepers = {
		db_id = google_sql_database.tutordb.id
		userassets_id = google_storage_bucket.userassets.id
		exports_id = google_storage_bucket.exports.id
		imports_id = google_storage_bucket.imports.id
	}
}

data "cloudinit_config" "backend" {
	gzip = false
	base64_encode = false

	part {
		content_type = "text/cloud-config"
		content = templatefile("backend-gce/cloudinit.yml.tmpl", {
			container_image = data.google_container_registry_image.backend.image_url
			buckets = {
				userassets = google_storage_bucket.userassets.name
				exports = google_storage_bucket.exports.name
				imports = google_storage_bucket.imports.name
			}
			db = {
				host = google_sql_database_instance.default.private_ip_address
				username = google_sql_user.tutordb.name
				password = google_sql_user.tutordb.password
				name = google_sql_database.tutordb.name
			}
			fenix_oauth = {
				id = var.fenix_oauth_id
				secret = var.fenix_oauth_secret
				callback_url = "https://${local.dns_root}/login"
			}
			auth = {
				cookie_domain = local.dns_root
				secret = random_password.auth_secret.result
			}
		})
	}
}

resource "google_compute_instance_template" "backend" {
	name_prefix = "backend-${random_string.suffix.result}-"
	machine_type = "e2-highcpu-2"

	disk {
		source_image = data.google_compute_image.backend.self_link
	}

	network_interface {
		subnetwork = google_compute_subnetwork.backend_default.self_link
	}

	service_account {
		email = google_service_account.backend.email

		# IAM bindings are not enough for GCR/GCS it seems
		scopes = [
			"https://www.googleapis.com/auth/logging.write",
			"https://www.googleapis.com/auth/monitoring.write",
			"userinfo-email",
			"compute-ro",
			"storage-rw"
		]
	}

	metadata = {
		user-data = data.cloudinit_config.backend.rendered
	}

	lifecycle {
		create_before_destroy = true
	}
}

resource "google_compute_health_check" "backend" {
	name = "backend-http-health-check-${random_string.suffix.result}"

	timeout_sec = 1
	check_interval_sec = 1

	http_health_check {
		port = 80
		request_path = "/auth/demo/student"
	}
}

resource "google_compute_instance_group_manager" "backend" {
	name = "backend-igm"
	base_instance_name = "backend-igm"
	zone = "europe-west1-b"
	target_size = 1 # TODO: autoscale

	version {
		name = "backend-rolling"
		instance_template = google_compute_instance_template.backend.id
	}

	named_port {
		name = "http"
		port = 80
	}

	update_policy {
		type = "PROACTIVE"
		minimal_action = "RESTART"
		max_unavailable_fixed = 1
	}

	auto_healing_policies {
		health_check = google_compute_health_check.backend.id
		initial_delay_sec = 10 * 60 # 10 min
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

resource "google_compute_backend_service" "backend" {
	name = "backend-lbal-backend-${random_string.suffix.result}"
	health_checks = [google_compute_health_check.backend.id]

	backend {
		group = google_compute_instance_group_manager.backend.instance_group
	}

	depends_on = [google_compute_instance_group_manager.backend]
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

	host_rule {
		hosts = ["backend.${local.dns_root}"]
		path_matcher = "backend"
	}

	path_matcher {
		name = "backend"
		default_service = google_compute_backend_service.backend.id
		header_action {
			response_headers_to_add {
				header_name = "Access-Control-Allow-Origin"
				header_value = "https://${local.dns_root}, https://www.${local.dns_root}"
				replace = true
			}
			response_headers_to_add {
				header_name = "Access-Control-Allow-Credentials"
				header_value = "true"
				replace = true
			}
			response_headers_to_add {
				header_name = "Access-Control-Max-Age"
				header_value = 7 * 24 * 3600 # one week
				replace = true
			}
			response_headers_to_add {
				header_name = "Expect-CT"
				header_value= "max-age=86400, enforce"
				replace = false
			}
		}
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
