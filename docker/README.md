# Spoolman Docker Configuration Files with HTTPS Reverse Proxy

## Introduction

This is a simple Docker configuration for Spoolman and Caddy.

Caddy is used as a reverse proxy to offer HTTPS access to Spoolman.
This HTTPS access is required by Android Apps.

This configuration uses a self-signed certificate. You'll need to enable self-signed certificates in the config screen of the Android Application.

## Installation

1. Install Docker on your Spoolman host.

2. Choose a hostname (not an IP address) that is known from the Spoolman host and the phone running Bambu Spool Pal.

3. Replace `spoolman.local` in the `Caddyfile` with the hostname you've chosen in step 2.

4. Run the following command to check for any error messages:
   ```bash
   docker-compose up --build
   ```

5. You can use the following command to shut down the containers:
   ```bash
   docker-compose down
   ```

6. And use this command to start the containers in detached mode (in the background):
   ```bash
   docker-compose up -d
   ```

