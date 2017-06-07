let map = undefined;
let infowindow = undefined;

function filter() {
  readApartments(map.getBounds());
}

function initMap() {
  map = new google.maps.Map(document.getElementById('map'), {
    zoom: 13,
    center: {lat: 48.143, lng: 17.107},
    disableDefaultUI: true,
    zoomControl: true
  });
  infowindow = new google.maps.InfoWindow();

  map.addListener('dragend', function(){ filter(); });
  map.addListener('zoom_changed', function(){ filter(); });

  google.maps.event.addListenerOnce(map, 'idle', function(){
    filter();
  });
}

function setMarkerClick(marker: google.maps.Marker, item: Rent) {
  marker.addListener('click', function() {
    infowindow.setContent(generateContent(item));
    infowindow.open(map, marker);
  });
}

let markersMap = new Map();
function plotMarkers(result: Array<Rent>) {
  infowindow.close();
  let newMarkers = new Map();
  result.forEach(function (item: Rent) {
    const marker = markersMap.get(item.link);
    if (marker) {
      setMarkerClick(marker, item);
      newMarkers.set(item.link, marker);
    } else {
      const marker = new google.maps.Marker({
        position: {lat: item.location.lat, lng: item.location.lon},
        map: map
      });
      setMarkerClick(marker, item);
      newMarkers.set(item.link, marker);
    }
  });

  markersMap.forEach(function (marker, id) {
    // remove markers which are not visible any more
    if (!newMarkers.get(id)) {
      marker.setMap(null);
    }
  });
  markersMap = newMarkers;
}