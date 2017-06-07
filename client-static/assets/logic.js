function isNumeric(n) {
    return !isNaN(parseFloat(n)) && isFinite(n);
}
function readApartments(bounds) {
    if (bounds) {
        let numericParams = '';
        const areaMin = document.getElementById('areaMin').value;
        if (isNumeric(areaMin)) {
            numericParams += `&areaMin=${areaMin}`;
        }
        const areaMax = document.getElementById('areaMax').value;
        if (isNumeric(areaMax)) {
            numericParams += `&areaMax=${areaMax}`;
        }
        const priceMin = document.getElementById('priceMin').value;
        if (isNumeric(priceMin)) {
            numericParams += `&priceMin=${priceMin}`;
        }
        const priceMax = document.getElementById('priceMax').value;
        if (isNumeric(priceMax)) {
            numericParams += `&priceMax=${priceMax}`;
        }
        const nelat = bounds.getNorthEast().lat();
        const nelon = bounds.getNorthEast().lng();
        const swlat = bounds.getSouthWest().lat();
        const swlon = bounds.getSouthWest().lng();
        const opts = { method: 'GET', headers: {} };
        const params = `latTop=${nelat}&latBottom=${swlat}&lonWest=${swlon}&lonEast=${nelon}` + numericParams;
        fetch(`https://fozix1n9gl.execute-api.eu-west-1.amazonaws.com/dev/apartments?${params}`, opts).then(function (response) {
            return response.json();
        }).then(function (body) {
            plotMarkers(body);
        });
    }
}
function generateContent(item) {
    return `
    <a target="_blank" href="${item.link}" style="text-decoration:none;">
    <img src="${item.image}" style="float: left;padding-right:5px;"><br>
    ${item.title}<br><br>
    €${parseInt(item.price)}, ${item.area}m<sup>2</sup>
    </a>
  `;
}
let map = undefined;
let infowindow = undefined;
function filter() {
    readApartments(map.getBounds());
}
function initMap() {
    map = new google.maps.Map(document.getElementById('map'), {
        zoom: 13,
        center: { lat: 48.143, lng: 17.107 },
        disableDefaultUI: true,
        zoomControl: true
    });
    infowindow = new google.maps.InfoWindow();
    map.addListener('dragend', function () { filter(); });
    map.addListener('zoom_changed', function () { filter(); });
    google.maps.event.addListenerOnce(map, 'idle', function () {
        filter();
    });
}
function setMarkerClick(marker, item) {
    marker.addListener('click', function () {
        infowindow.setContent(generateContent(item));
        infowindow.open(map, marker);
    });
}
let markersMap = new Map();
function plotMarkers(result) {
    infowindow.close();
    let newMarkers = new Map();
    result.forEach(function (item) {
        const marker = markersMap.get(item.link);
        if (marker) {
            setMarkerClick(marker, item);
            newMarkers.set(item.link, marker);
        }
        else {
            const marker = new google.maps.Marker({
                position: { lat: item.location.lat, lng: item.location.lon },
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
