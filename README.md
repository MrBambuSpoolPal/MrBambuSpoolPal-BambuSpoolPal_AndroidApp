# MrBambuSpoolPal-BambuSpoolPal_AndroidApp
An Android App to Scan Bambu Lab RFID tags and update them into Spoolman for effortless stock management.


The purpose of this application is to automate the tracking of [official Bambu Lab spools](https://us.store.bambulab.com/collections/bambu-lab-3d-printer-filament?skr=yes) with [Spoolman](https://github.com/Donkie/Spoolman/wiki).

Scan your spools with your NFC-enabled phone to extract an identifier, density, initial weight, and length.

Using the [Spoolman filament database](https://github.com/Donkie/SpoolmanDB) allows for a relevant selection of names for the filaments for referencing.

The current weight can be adjusted through buttons, the input field, the slider, or by AI recognition of values from a scale.

Once configured, access to the Spoolman APIs allows for automatic management of the filament and spool.
Through configuration, this communication can be automated with each weight modification.

For more information: [wiki](https://github.com/MrBambuSpoolPal/MrBambuSpoolPal-BambuSpoolPal_AndroidApp/wiki).
For any [contact or questions](https://github.com/MrBambuSpoolPal/MrBambuSpoolPal-BambuSpoolPal_AndroidApp/issues).

# Installation

## Spoolman

Set up a Spoolman instance. I use Docker.

Android needs HTTPS, so I use Caddy as a reverse proxy. [Spoolman FAQ](https://github.com/Donkie/Spoolman/wiki/Frequently-Asked-Questions)

A simple guide is available in the docker directory.

## Android App Installation

The App is available here: [BambuSpoolTag-v1.0RC](https://github.com/MrBambuSpoolPal/MrBambuSpoolPal-BambuSpoolPal_AndroidApp/tree/bst_v1_0/releases)

You need to allow **unknown sources**.
The app uses **the internet** to download Spoolman's database and call Spoolman's API.
The app needs NFC to scan the **spool's** RFID tag.
The app can use the camera to scan the weight from a scale using **AI** and number recognition.

## Android App Setup

On the very first run, you need to fetch the Spoolman **database** from the configuration screen (small gear at the top). You should see about 200 filaments.

You need to **set up** the Spoolman API endpoint. If you use my Caddy configuration, you need to allow self-signed certificates from the configuration screen.

# How to use it

First, scan a spool. Please stay at least 2 seconds to avoid **reading errors**.
Adjust the color name from the list. The percentage is a pattern matching score between the RFID **information** and the database.
Adjust the weight for the actual **spool** (filament + spool).
Hit "update" to store the filament and spool in Spoolman.