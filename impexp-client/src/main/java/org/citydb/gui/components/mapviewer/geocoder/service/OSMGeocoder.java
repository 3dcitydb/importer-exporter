package org.citydb.gui.components.mapviewer.geocoder.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.citydb.config.i18n.Language;
import org.citydb.gui.components.mapviewer.geocoder.GeocoderResult;
import org.citydb.gui.components.mapviewer.geocoder.Location;
import org.citydb.gui.components.mapviewer.geocoder.LocationType;
import org.jdesktop.swingx.mapviewer.GeoPosition;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class OSMGeocoder implements GeocodingService {

    @Override
    public GeocoderResult geocode(String address) throws GeocodingServiceException {
        String serviceCall;
        try {
            serviceCall = "https://nominatim.openstreetmap.org/search?" +
                    "q=" + URLEncoder.encode(address, StandardCharsets.UTF_8.displayName()) +
                    "&format=jsonv2";

            // add language parameter
            String language = Language.I18N.getLocale().getLanguage();
            if (!language.isEmpty())
                serviceCall += "&accept-language=" + language;

        } catch (UnsupportedEncodingException e) {
            throw new GeocodingServiceException("Failed to construct the geocoding service call.", e);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                openConnection(serviceCall).getInputStream(), StandardCharsets.UTF_8))) {
            JsonElement element = new Gson().fromJson(reader, JsonElement.class);
            GeocoderResult geocodingResult = new GeocoderResult();

            if (element.isJsonArray()) {
                JsonArray places = element.getAsJsonArray();
                for (int i = 0; i < places.size(); i++)
                    geocodingResult.addLocation(parsePlace(places.get(i)));
            }

            return geocodingResult;
        } catch (IOException e) {
            throw new GeocodingServiceException("Failed to invoke the geocoding service.", e);
        } catch (Exception e) {
            throw new GeocodingServiceException("Failed to parse the geocoding service response.", e);
        }
    }

    @Override
    public GeocoderResult lookupAddress(GeoPosition latlon, int zoomLevel) throws GeocodingServiceException {
        String serviceCall = "https://nominatim.openstreetmap.org/reverse?" +
                "lat=" + latlon.getLatitude() + "&lon=" + latlon.getLongitude() +
                "&format=jsonv2" +
                "&zoom=" + Math.min(18, 18 - zoomLevel + 2);

        // add language parameter
        String language = Language.I18N.getLocale().getLanguage();
        if (!language.isEmpty())
            serviceCall += "&accept-language=" + language;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(openConnection(serviceCall).getInputStream(), StandardCharsets.UTF_8))) {
            JsonElement element = new Gson().fromJson(reader, JsonElement.class);
            GeocoderResult geocodingResult = new GeocoderResult();
            geocodingResult.addLocation(parsePlace(element));

            return geocodingResult;
        } catch (IOException e) {
            throw new GeocodingServiceException("Failed to invoke the geocoding service.", e);
        } catch (Exception e) {
            throw new GeocodingServiceException("Failed to parse the geocoding service response.", e);
        }
    }

    private Location parsePlace(JsonElement element) throws Exception {
        Location location = null;

        if (element.isJsonObject()) {
            JsonObject place = element.getAsJsonObject();
            location = new Location();

            location.setFormattedAddress(place.get("display_name").getAsString());

            double lat = place.get("lat").getAsDouble();
            double lon = place.get("lon").getAsDouble();
            GeoPosition position = new GeoPosition(lat, lon);
            location.setPosition(position);

            JsonArray bbox = place.get("boundingbox").getAsJsonArray();
            GeoPosition southWest = new GeoPosition(bbox.get(0).getAsDouble(), bbox.get(2).getAsDouble());
            GeoPosition northEast = new GeoPosition(bbox.get(1).getAsDouble(), bbox.get(3).getAsDouble());
            location.setViewPort(northEast, southWest);

            location.addAttribute(place.get("category").getAsString(), place.get("type").getAsString());

            LocationType locationType = LocationType.APPROXIMATE;
            if (location.hasAttribute("place") && location.getAttribute("place").equals("house")
                    || location.hasAttribute("building"))
                locationType = LocationType.PRECISE;

            location.setLocationType(locationType);
        }

        return location;
    }

    private URLConnection openConnection(String serviceCall) throws IOException {
        URLConnection connection = new URL(serviceCall).openConnection();
        connection.setRequestProperty("User-Agent", "3DCityDB-Importer-Exporter/4.0");
        return connection;
    }

}