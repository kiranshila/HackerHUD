(ns hackerhud.weather
  (:require [hackerhud.utils :as utils]))

(def lat 27.984)
(def lon -82.463)

(def onecall-excludes "minutely,hourly,daily")

(goog-define weather-api-key "")
(goog-define aqi-api-key "")

(def current-weather (atom nil))

(defn weather-one-call-api [lat lon exclude api-key]
  (str "http://api.openweathermap.org/data/2.5/onecall?"
       "lat=" lat
       "&lon=" lon
       "&exclude=" exclude
       "&appid=" api-key))

(defn aqi-call-api [lat lon api-key]
  (str "http://api.waqi.info/feed/geo:"
       lat ";"
       lon "/?token="
       api-key))

(def degree 0xF8)

; Redefine weather characteristics within 20 col vfd limit
(def ids {200 "T-Storm / Light Rain"
          201 "T-Storm / Rain"
          202 "T-Storm / Heavy Rain"
          210 "Light T-Storm"
          211 "Thunderstorms"
          212 "Heavy Thunderstorms"
          221 "Ragged Thunderstorms"
          230 "T-Storm / Light Drzl"
          231 "T-Storm / Drizzle"
          232 "T-Storm / Heavy Drzl"

          300 "Light Drizzle"
          301 "Drizzle"
          302 "Heavy Drizzle"
          310 "Light Drizzle / Rain"
          311 "Drizzle / Rain"
          312 "Heavy Drizzle Rain"
          313 "Shower Rain / Drzl"
          314 "Hvy Shwr Rain / Drzl"
          321 "Shower Drizzle"

          500 "Light Rain"
          501 "Moderate Rain"
          502 "Heavy Rain"
          503 "Very Heavy Rain"
          504 "Extreme Rain"
          511 "Freezing Rain"
          520 "Light Shower Rain"
          521 "Shower Rain"
          522 "Heavy Shower Rain"
          531 "Ragged Shower Rain"

          600 "Light Snow"
          601 "Snow"
          602 "Heavy Snow"
          611 "Sleet"
          612 "Light Shower Sleet"
          613 "Shower Sleet"
          615 "Light Rain and Snow"
          616 "Rain and Snow"
          620 "Light Shower Snow"
          621 "Shower Snow"
          622 "Heavy Shower Snow"

          700 "Mist"
          711 "Smoke"
          721 "Haze"
          731 "Sand and Dust Whirls"
          741 "Fog"
          751 "Sand"
          761 "Dust"
          762 "Volcanic Ash"
          771 "Squalls"
          781 "Tornado"

          800 "Clear"
          801 "Few Clouds"
          802 "Scattered Clouds"
          803 "Broken Clouds"
          804 "Overcast Clouds"})

(defn uv-index [uvi]
  (cond
    (<= uvi 2) "Low"
    (<= uvi 5) "Moderate"
    (<= uvi 7) "High"
    (<= uvi 10) "Very High"
    :finally "Extreme"))

(defn aq-index [aqi]
  (cond
    (<= aqi 50) "Good"
    (<= aqi 100) "Moderate"
    (<= aqi 150) "Bad"
    (<= aqi 200) "Unhealthy"
    (<= aqi 300) "Very Unhealthy"
    :finally "Hazardous"))

(defn K-to-F [K]
  (- (* K (/ 9 5)) 459.67))

(defn update-weather [resp]
  (when resp
    (let [resp (.-state resp)
          current (.-current resp)
         weather (get (.-weather current) 0)
         feels-like (.-feels_like current)
         uvi (.-uvi current)
         id (.-id weather)]
     (swap! current-weather
            merge
            {:uvi (uv-index uvi)
             :description (get ids id)
             :feels-like (.round js/Math (K-to-F feels-like))}))))

(defn update-aqi [resp]
  (when resp
    (let [resp (.-state resp)
          data (.-data resp)
          aqi (.-aqi data)]
      (swap! current-weather merge {:aqi (aq-index aqi)}))))

(defn get-new-weather
  "Perform a GET to the weather API to update current weather"
  []
  (.. (utils/get-uri (weather-one-call-api lat lon onecall-excludes weather-api-key))
      (then update-weather)))

(defn get-new-aqi
  []
  (.. (utils/get-uri (aqi-call-api lat lon aqi-api-key))
      (then update-aqi)))
