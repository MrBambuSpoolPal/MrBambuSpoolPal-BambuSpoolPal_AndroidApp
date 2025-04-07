FROM caddy:latest

# Update the package index and install the 'libnss3-tools' package.
# Afterwards, remove the package list files to reduce the image size.
RUN apk update && apk install -y libnss3-tools && rm -rf /var/lib/apt/lists/*
