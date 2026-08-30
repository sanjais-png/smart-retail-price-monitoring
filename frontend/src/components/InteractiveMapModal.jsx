import React, { useState, useEffect } from 'react';
import { MapContainer, TileLayer, Marker, useMapEvents, useMap } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import { MapPin, Search, CheckCircle2, X } from 'lucide-react';

// Custom Map Pin Icon
const pinIcon = new L.Icon({
  iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-green.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41]
});

// Component to handle map clicks & move center
const MapEventsHandler = ({ onLocationSelected }) => {
  useMapEvents({
    click(e) {
      onLocationSelected(e.latlng.lat, e.latlng.lng);
    }
  });

  return null;
};

// Component to programmatically re-center map & trigger invalidateSize
const ChangeView = ({ center }) => {
  const map = useMap();
  useEffect(() => {
    if (center) {
      map.setView(center, 13);
    }
    const timer = setTimeout(() => {
      map.invalidateSize();
    }, 200);
    return () => clearTimeout(timer);
  }, [center, map]);
  return null;
};

export const InteractiveMapModal = ({ isOpen = true, onClose, onSelectLocation }) => {
  const [position, setPosition] = useState([11.0168, 76.9558]); // Default: Coimbatore, TN
  const [mapCenter, setMapCenter] = useState([11.0168, 76.9558]);
  const [searchQuery, setSearchQuery] = useState('');
  const [locationDetails, setLocationDetails] = useState({
    state: 'Tamil Nadu',
    district: 'Coimbatore',
    address: 'Coimbatore, Tamil Nadu',
    lat: 11.0168,
    lng: 76.9558
  });
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (isOpen) {
      reverseGeocode(position[0], position[1]);
    }
  }, [isOpen]);

  const reverseGeocode = async (lat, lng) => {
    setLoading(true);
    try {
      const res = await fetch(`https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}&zoom=14`)
        .then(r => r.json());
      
      let state = 'Tamil Nadu';
      let district = 'Coimbatore';
      let fullAddr = `${lat.toFixed(4)}, ${lng.toFixed(4)}`;

      if (res && res.address) {
        const a = res.address;
        state = a.state || state;
        district = a.city || a.town || a.county || a.state_district || a.subdistrict || district;
        fullAddr = res.display_name || `${district}, ${state}`;
      }

      const details = {
        state,
        district,
        address: fullAddr,
        lat,
        lng
      };

      setLocationDetails(details);
    } catch (e) {
      console.warn('Geocode error', e);
    } finally {
      setLoading(false);
    }
  };

  const handleMapClick = (lat, lng) => {
    setPosition([lat, lng]);
    reverseGeocode(lat, lng);
  };

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!searchQuery.trim()) return;

    setLoading(true);
    try {
      const res = await fetch(`https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(searchQuery + ', India')}`)
        .then(r => r.json());

      if (res && res.length > 0) {
        const top = res[0];
        const lat = parseFloat(top.lat);
        const lon = parseFloat(top.lon);

        setPosition([lat, lon]);
        setMapCenter([lat, lon]);
        reverseGeocode(lat, lon);
      } else {
        alert('Location not found. Try searching a city name like "Coimbatore" or "Chennai".');
      }
    } catch (e) {
      console.warn('Search error', e);
    } finally {
      setLoading(false);
    }
  };

  const handleConfirm = () => {
    if (onSelectLocation) {
      onSelectLocation(locationDetails);
    }
    if (onClose) onClose();
  };

  if (!isOpen) return null;

  return (
    <div style={{
      position: 'fixed',
      top: 0,
      left: 0,
      right: 0,
      bottom: 0,
      backgroundColor: 'rgba(15, 23, 42, 0.7)',
      backdropFilter: 'blur(4px)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      zIndex: 2000
    }}>
      <div style={{
        backgroundColor: '#ffffff',
        borderRadius: '24px',
        width: '90%',
        maxWidth: '900px',
        overflow: 'hidden',
        boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.3)',
        display: 'flex',
        flexDirection: 'column',
        maxHeight: '90vh'
      }}>
        {/* Header */}
        <div style={{
          padding: '20px 24px',
          borderBottom: '1px solid #e2e8f0',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          backgroundColor: '#f8fafc'
        }}>
          <div>
            <h3 style={{ fontSize: '18px', fontWeight: 800, color: '#0f172a', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <MapPin size={22} color="#166534" />
              <span>Select Location from Interactive Map</span>
            </h3>
            <p style={{ fontSize: '12px', color: '#64748b', marginTop: '2px' }}>
              Click anywhere on the map or search your area to auto-detect State, District, and Shop address.
            </p>
          </div>

          <button
            onClick={onClose}
            style={{ border: 'none', background: 'none', cursor: 'pointer', color: '#64748b' }}
          >
            <X size={24} />
          </button>
        </div>

        {/* Map Search Bar */}
        <div style={{ padding: '14px 24px', backgroundColor: '#ffffff', borderBottom: '1px solid #f1f5f9' }}>
          <form onSubmit={handleSearch} style={{ display: 'flex', gap: '10px' }}>
            <div style={{ flex: 1, position: 'relative' }}>
              <input
                type="text"
                placeholder="Search city, area, or landmark (e.g. Gandhipuram, Coimbatore)..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                style={{
                  width: '100%',
                  padding: '10px 14px 10px 38px',
                  borderRadius: '10px',
                  border: '1px solid #cbd5e1',
                  fontSize: '13px',
                  outline: 'none'
                }}
              />
              <Search size={16} color="#94a3b8" style={{ position: 'absolute', left: '12px', top: '12px' }} />
            </div>

            <button
              type="submit"
              disabled={loading}
              className="btn-emerald"
              style={{ padding: '10px 20px', borderRadius: '10px', fontSize: '13px' }}
            >
              {loading ? 'Searching...' : 'Search Location'}
            </button>
          </form>
        </div>

        {/* Leaflet Interactive Map Container */}
        <div style={{ height: '420px', width: '100%', position: 'relative' }}>
          <MapContainer center={mapCenter} zoom={13} style={{ height: '100%', width: '100%' }}>
            <TileLayer
              attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
              url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            />
            <Marker position={position} icon={pinIcon} />
            <MapEventsHandler onLocationSelected={handleMapClick} />
            <ChangeView center={mapCenter} />
          </MapContainer>
        </div>

        {/* Selected Location Info & Confirm Bar */}
        <div style={{
          padding: '20px 24px',
          backgroundColor: '#f0fdf4',
          borderTop: '1px solid #bbf7d0',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          gap: '20px'
        }}>
          <div>
            <div style={{ fontSize: '12px', fontWeight: 700, color: '#166534', textTransform: 'uppercase' }}>
              Pinpointed Location Info
            </div>
            <div style={{ fontSize: '15px', fontWeight: 800, color: '#0f172a', marginTop: '2px' }}>
              📍 {locationDetails.district}, {locationDetails.state}
            </div>
            <div style={{ fontSize: '12px', color: '#475569', marginTop: '2px', maxWidth: '520px', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
              {locationDetails.address} (GPS: {locationDetails.lat.toFixed(4)}, {locationDetails.lng.toFixed(4)})
            </div>
          </div>

          <button
            onClick={handleConfirm}
            className="btn-emerald"
            style={{
              padding: '12px 24px',
              borderRadius: '12px',
              fontSize: '14px',
              fontWeight: 800,
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              whiteSpace: 'nowrap',
              boxShadow: '0 4px 14px rgba(22, 101, 52, 0.3)'
            }}
          >
            <CheckCircle2 size={18} />
            <span>Confirm & Auto-Fill Details</span>
          </button>
        </div>

      </div>
    </div>
  );
};
