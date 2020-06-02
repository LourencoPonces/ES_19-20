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
}

resource "google_storage_bucket_access_control" "tf_state_tf_svc" {
	bucket = google_storage_bucket.tf_state.name
	role = "OWNER"
	entity = "user-terraform@quizzestutor.iam.gserviceaccount.com"
}

terraform {
	backend "gcs" {
		bucket = "quizzestutor-tf-state"
		credentials = "credentials.json"
	}
}
