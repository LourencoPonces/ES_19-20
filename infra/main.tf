locals {
	dns_root = "quizzes-tutor.breda.pt"
	project = "quizzestutor"
	region = "europe-west1"
	zone = "europe-west1-b"
}

variable "app_version" {
	type = string
	description = "version to deploy (stable, staging, last, some commit hash, etc.)"
	default = "last"
}

variable "atlantis_version" {
	type = string
	description = "Atlantis version to deploy"
	default = "v0.13.0"
}

provider "google" {
	version = "~> 3.24"
	credentials = file("credentials.json")
	region = local.region
	project = local.project
}

provider "random" {
	version = "~> 2.2"
}

provider "cloudinit" {
	version = "~> 1.0"
}

resource "google_storage_bucket" "tf_state" {
	name = "quizzestutor-tf-state"
	location = local.region
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

resource "google_container_registry" "default" {
	location = "EU"
}

# DNS

resource "google_dns_managed_zone" "default" {
	name = "quizzestutor-dns-zone-${random_string.suffix.result}"
	dns_name = "${local.dns_root}."

	lifecycle {
		prevent_destroy = true # may require NS record updates after recreation
	}
}

resource "google_dns_record_set" "google_domain_verification" {
	managed_zone = google_dns_managed_zone.default.name
	name = "jy3vqdvtaxmj.${google_dns_managed_zone.default.dns_name}"
	type = "CNAME"
	ttl = 300

	rrdatas = ["gv-2vwpeccsnzqlb2.dv.googlehosted.com."]
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

resource "google_dns_record_set" "atlantis" {
	for_each = {
		"IPV4" = "A"
		"IPV6" = "AAAA"
	}

	managed_zone = google_dns_managed_zone.default.name
	name = "atlantis.${google_dns_managed_zone.default.dns_name}"
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

resource "google_compute_router" "backend" {
	name = "backend-router"
	region = google_compute_subnetwork.backend_default.region
	network = google_compute_network.backend.id
}

resource "google_compute_router_nat" "backend" {
	name = "${google_compute_router.backend.name}-nat"
	router = google_compute_router.backend.name
	region = google_compute_router.backend.region
	nat_ip_allocate_option = "AUTO_ONLY"
	source_subnetwork_ip_ranges_to_nat = "ALL_SUBNETWORKS_ALL_IP_RANGES"

	log_config {
		enable = true
		filter = "ERRORS_ONLY"
	}
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

resource "google_compute_network" "ops" {
	name = "ops"
}

resource "google_compute_router" "ops" {
	name = "ops-router"
	network = google_compute_network.ops.id
}

resource "google_compute_router_nat" "ops" {
	name = "${google_compute_router.ops.name}-nat"
	router = google_compute_router.ops.name
	region = google_compute_router.ops.region
	nat_ip_allocate_option = "AUTO_ONLY"
	source_subnetwork_ip_ranges_to_nat = "ALL_SUBNETWORKS_ALL_IP_RANGES"

	log_config {
		enable = true
		filter = "ERRORS_ONLY"
	}
}

resource "google_compute_firewall" "ops_allow_icmp_ssh_http" {
	name = "ops-allow-icmp-ssh-http"
	network = google_compute_network.ops.id

	allow {
		protocol = "icmp"
	}

	allow {
		protocol = "tcp"
		ports = ["80", "22"]
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
	keepers = {
		db = google_sql_database.tutordb.self_link
	}
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
	location = local.region
	storage_class = "REGIONAL"
	bucket_policy_only = true

	lifecycle {
		prevent_destroy = true
	}

	cors {
		origin = ["https://${local.dns_root}", "https://www.${local.dns_root}"]
		method = ["GET", "OPTIONS", "HEAD"]
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
	role = "roles/storage.objectAdmin"
}

resource "google_storage_bucket" "exports" {
	name = "quizzestutor-exports-${random_string.suffix.result}"
	location = local.region
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
	role = "roles/storage.objectAdmin"
}

resource "google_storage_bucket" "imports" {
	name = "quizzestutor-imports-${random_string.suffix.result}"
	location = local.region
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
	role = "roles/storage.objectAdmin"
}

data "google_compute_image" "backend" {
	family = "cos-81-lts"
	project = "gce-uefi-images"
}

resource "google_storage_bucket_iam_member" "gcr_backend" {
	bucket = google_container_registry.default.id
	role = "roles/storage.objectViewer"
	member = "serviceAccount:${google_service_account.backend.email}"
}

data "google_container_registry_image" "backend" {
	name = "backend-gce"
	region = "eu"
	tag = var.app_version
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

data "google_secret_manager_secret_version" "fenix_id" {
	secret = "FENIX_ID"
}

data "google_secret_manager_secret_version" "fenix_secret" {
	secret = "FENIX_SECRET"
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
				id = data.google_secret_manager_secret_version.fenix_id.secret_data
				secret = data.google_secret_manager_secret_version.fenix_secret.secret_data
				callback_url = "https://${local.dns_root}/login"
			}
			auth = {
				cookie_domain = local.dns_root
				secret = random_password.auth_secret.result
			}
			allowed_origins = [
				"https://${local.dns_root}",
				"https://www.${local.dns_root}"
			]
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

	depends_on = [
		google_storage_bucket_iam_binding.userassets_backend,
		google_storage_bucket_iam_binding.exports_backend,
		google_storage_bucket_iam_binding.imports_backend
	]
}

resource "google_compute_health_check" "backend" {
	name = "backend-http-health-check-${random_string.suffix.result}"

	timeout_sec = 2
	check_interval_sec = 2
	unhealthy_threshold = 10

	http_health_check {
		port = 80
		request_path = "/auth/demo/student"
	}
}

resource "google_compute_instance_group_manager" "backend" {
	name = "backend-igm"
	base_instance_name = "backend-igm"
	zone = local.zone
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

# Frontend
data "google_container_registry_image" "frontend" {
	name = "frontend-gc"
	region = "eu"
	tag = var.app_version
}

resource "google_cloud_run_service" "frontend" {
	name     = "quizzestutor-frontend"
	location = local.region

	metadata {
		namespace = local.project
	}

	template {
		spec {
			containers {
				image = data.google_container_registry_image.frontend.image_url
			}
		}
	}
}

resource "google_cloud_run_service_iam_member" "frontend" {
	service = google_cloud_run_service.frontend.name
	location = google_cloud_run_service.frontend.location
	member = "allUsers"
	role = "roles/run.invoker"
}

resource "google_cloud_run_domain_mapping" "frontend_default" {
	name     = local.dns_root
	location = local.region

	metadata {
		namespace = local.project
	}

	spec {
		route_name = google_cloud_run_service.frontend.name
	}
}

resource "google_dns_record_set" "frontend_default" {
	# TODO: try to define in terms of google_cloud_run_domain_mapping status
	for_each = {
		_ = {
			type = "A"
			rrdatas = [
				"216.239.32.21",
				"216.239.34.21",
				"216.239.36.21",
				"216.239.38.21"
			]
		}
		__ = {
			type = "AAAA"
			rrdatas = [
				"2001:4860:4802:32::15",
				"2001:4860:4802:34::15",
				"2001:4860:4802:36::15",
				"2001:4860:4802:38::15"
			]
		}
	}

	managed_zone = google_dns_managed_zone.default.name
	name = google_dns_managed_zone.default.dns_name
	type = lookup(each.value, "type", "A")
	ttl = 300

	rrdatas = each.value.rrdatas
}

resource "google_cloud_run_domain_mapping" "frontend_www" {
	name     = "www.${local.dns_root}"
	location = local.region

	metadata {
		namespace = local.project
	}

	spec {
		route_name = google_cloud_run_service.frontend.name
	}
}

resource "google_dns_record_set" "frontend_www" {
	# TODO: try to define in terms of google_cloud_run_domain_mapping status
	managed_zone = google_dns_managed_zone.default.name
	name = "www.${google_dns_managed_zone.default.dns_name}"
	type = "CNAME"
	ttl = 300

	rrdatas = ["ghs.googlehosted.com."]
}

# Atlantis (CI/CD)
resource "google_service_account" "atlantis" {
	account_id = "atlantis-${random_string.suffix.result}"
}

resource "google_storage_bucket" "atlantis_data" {
	name = "quizzestutor-atlantis-data"
	location = local.region
	storage_class = "REGIONAL"
	bucket_policy_only = true
}

resource "google_storage_bucket_iam_binding" "atlantis_data" {
	bucket = google_storage_bucket.atlantis_data.name
	members = ["serviceAccount:${google_service_account.atlantis.email}"]
	role = "roles/storage.objectAdmin"
}

resource "google_secret_manager_secret_iam_member" "atlantis" {
	for_each = toset([
		"ATLANTIS_GH_USER",
		"ATLANTIS_GH_TOKEN",
		"ATLANTIS_WEBHOOK_SECRET"
	])

	secret_id = each.value
	member = "serviceAccount:${google_service_account.atlantis.email}"
	role = "roles/secretmanager.secretAccessor"
}

data "google_container_registry_image" "atlantis" {
	name = "atlantis-gcp"
	region = "eu"
	tag = var.atlantis_version
}

resource "google_storage_bucket_iam_member" "gcr_atlantis" {
	bucket = google_container_registry.default.id
	role = "roles/storage.objectViewer"
	member = "serviceAccount:${google_service_account.atlantis.email}"
}

data "google_compute_image" "atlantis" {
	family = "cos-81-lts"
	project = "gce-uefi-images"
}

data "cloudinit_config" "atlantis" {
	gzip = false
	base64_encode = false

	part {
		content_type = "text/cloud-config"
		content = templatefile("atlantis-gcp/cloudinit.yml.tmpl", {
			container_image = data.google_container_registry_image.atlantis.image_url
			data_bucket = google_storage_bucket.atlantis_data.name
			base_url = "https://atlantis.${local.dns_root}/"
			svc_account_email = google_service_account.atlantis.email
		})
	}
}


resource "google_compute_instance_template" "atlantis" {
	name_prefix = "atlantis-${random_string.suffix.result}-"
	machine_type = "e2-highcpu-2"

	disk {
		source_image = data.google_compute_image.atlantis.self_link
	}

	network_interface {
		# needs only to access the world
		network = google_compute_network.ops.name
	}

	service_account {
		email = google_service_account.atlantis.email

		# IAM bindings are not enough for GCR/GCS it seems
		scopes = [
			"https://www.googleapis.com/auth/logging.write",
			"https://www.googleapis.com/auth/monitoring.write",
			"userinfo-email",
			"compute-ro",
			"storage-rw",
			"cloud-platform"
		]
	}

	metadata = {
		user-data = data.cloudinit_config.atlantis.rendered
	}

	lifecycle {
		create_before_destroy = true
	}

	depends_on = [
		google_storage_bucket_iam_binding.atlantis_data,
		google_secret_manager_secret_iam_member.atlantis
	]
}

resource "google_compute_health_check" "atlantis" {
	name = "atlantis-http-health-check-${random_string.suffix.result}"

	http_health_check {
		port = 80
	}
}

resource "google_compute_instance_group_manager" "atlantis" {
	name = "atlantis-igm"
	base_instance_name = "atlantis-ig"
	zone = local.zone
	target_size = 1

	version {
		name = "atlantis-rolling"
		instance_template = google_compute_instance_template.atlantis.id
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
		health_check = google_compute_health_check.atlantis.id
		initial_delay_sec = 10 * 60 # 10 min
	}
}

resource "google_compute_backend_service" "atlantis" {
	name = "atlantis-lbal-backend-${random_string.suffix.result}"
	health_checks = [google_compute_health_check.atlantis.id]

	backend {
		group = google_compute_instance_group_manager.atlantis.instance_group
	}

	depends_on = [google_compute_instance_group_manager.atlantis]
}

resource "google_compute_url_map" "frontend_lbal" {
	name = "frontend-lbal-url-map-${random_string.suffix.result}"

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
	}

	host_rule {
		hosts = ["atlantis.${local.dns_root}"]
		path_matcher = "atlantis"
	}

	path_matcher {
		name = "atlantis"
		default_service = google_compute_backend_service.atlantis.id
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
	ssl_certificates = ["manual-quizzestutor-cert-rbsyk4x4"]
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
