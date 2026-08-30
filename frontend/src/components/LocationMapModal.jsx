import React, { useState, useEffect } from 'react';
import { MapContainer, TileLayer, Marker, useMapEvents, useMap } from 'react-leaflet';
import L from 'leaflet';
import { MapPin, Search, Check, X, Loader2 } from 'lucide-react';
import 'leaflet/dist/leaflet.css';

// Fix default marker icon issue in Leaflet with Vite/React
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
});

// Component to handle map clicks & center updates
const MapController = ({ position, setPosition, onReverseGeocode }) => {
  const map = useMap();

  useEffect(() => {
    if (position) {
      map.flyTo(position, 13, { duration: 1.2 });
    }
  }, [position, map]);

  useMapEvents({
    click(e) {
      const newPos = [e.latlng.lat, e.latlng.lng];
      setPosition(newPos);
      onReverseGeocode(e.latlng.lat, e.latlng.lng);
    }
  });

  return position ? (
    <Marker
      position={position}
      draggable={true}
      eventHandlers={{
        dragend(e) {
          const marker = e.target;
          const pos = marker.getLatLng();
          setPosition([pos.lat, pos.lng]);
          onReverseGeocode(pos.lat, pos.lng);
        }
      }}
    />
  ) : null;
};

export const LocationMapModal = ({ initialLat, initialLng, initialAddress, isOpen, onClose, onSelectLocation }) => {
  const defaultCenter = initialLat && initialLng ? [initialLat, initialLng] : [11.0168, 76.9558]; // Default Coimbatore, TN
  const [position, setPosition] = useState(defaultCenter);
  const [selectedAddress, setSelectedAddress] = useState(initialAddress || 'Coimbatore, Tamil Nadu');
  const [searchQuery, setSearchQuery] = useState('');
  const [isSearching, setIsSearching] = useState(false);
  const [isGeocoding, setIsGeocoding] = useState(false);

  useEffect(() => {
    if (initialLat && initialLng) {
      setPosition([initialLat, initialLng]);
    }
    if (initialAddress) {
      setSelectedAddress(initialAddress);
    }
  }, [initialLat, initialLng, initialAddress]);

  if (!isOpen) return null;

  const handleReverseGeocode = async (lat, lon) => {
    setIsGeocoding(true);
    try {
      const res = await fetch(`https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lon}`);
      const data = await res.json();
      if (data && data.display_name) {
        setSelectedAddress(data.display_name);
      } else {
        setSelectedAddress(`Location (${lat.toFixed(4)}, ${lon.toFixed(4)})`);
      }
    } catch (e) {
      setSelectedAddress(`Location (${lat.toFixed(4)}, ${lon.toFixed(4)})`);
    } finally {
      setIsGeocoding(false);
    }
  };

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!searchQuery.trim()) return;
    setIsSearching(true);
    try {
      const res = await fetch(`https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(searchQuery)}&limit=1`);
      const data = await res.json();
      if (data && data.length > 0) {
        const item = data[0];
        const newLat = parseFloat(item.lat);
        const newLon = parseFloat(item.lon);
        setPosition([newLat, newLon]);
        setSelectedAddress(item.display_name);
      } else {
        alert('Location not found. Please try another search term.');
      }
    } catch (err) {
      alert('Search failed. Please check internet connection.');
    } finally {
      setIsSearching(false);
    }
  };

  const handleConfirm = () => {
    onSelectLocation({
      address: selectedAddress,
      latitude: Number(position[0].toFixed(6)),
      longitude: Number(position[1].toFixed(6))
    });
    onClose();
  };

  return (
    <div style={{
      position: 'fixed',
      top: 0,
      left: 0,
      right: 0,
      bottom: 0,
      backgroundColor: 'rgba(15, 23, 42, 0.65)',
      backdropFilter: 'blur(4px)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      zIndex: 2000,
      padding: '20px'
    }}>
      <div style={{
        backgroundColor: '#ffffff',
        borderRadius: '20px',
        maxWidth: '720px',
        width: '100%',
        boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.25)',
        display: 'flex',
        flexDirection: 'column',
        overflow: 'hidden',
        border: '1px solid #e2e8f0'
      }}>
        {/* Modal Header */}
        <div style={{
          padding: '20px 24px',
          borderBottom: '1px solid #e2e8f0',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          backgroundColor: '#f8fafc'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <MapPin size={22} color="#166534" />
            <h3 style={{ fontSize: '18px', fontWeight: 800, color: '#0f172a' }}>Choose Location From Map</h3>
          </div>
          <button
            onClick={onClose}
            style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#64748b' }}
          >
            <X size={20} />
          </button>
        </div>

        {/* Search Input Bar */}
        <div style={{ padding: '16px 24px', borderBottom: '1px solid #f1f5f9' }}>
          <form onSubmit={handleSearch} style={{ display: 'flex', gap: '8px' }}>
            <div style={{ position: 'relative', flex: 1 }}>
              <input
                type="text"
                placeholder="Search city, neighborhood, or landmark (e.g. Coimbatore, Gandhipuram)..."
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
              disabled={isSearching}
              style={{
                padding: '10px 20px',
                borderRadius: '10px',
                backgroundColor: '#166534',
                color: '#ffffff',
                border: 'none',
                fontWeight: 700,
                fontSize: '13px',
                cursor: 'pointer'
              }}
            >
              {isSearching ? 'Searching...' : 'Search'}
            </button>
          </form>
        </div>

        {/* Interactive Map View */}
        <div style={{ height: '340px', width: '100%', position: 'relative' }}>
          <MapContainer
            center={position}
            zoom={13}
            style={{ height: '100%', width: '100%' }}
          >
            <TileLayer
              attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
              url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            />
            <MapController
              position={position}
              setPosition={setPosition}
              onReverseGeocode={handleReverseGeocode}
            />
          </MapContainer>
        </div>

        {/* Selected Address Preview & Footer */}
        <div style={{ padding: '20px 24px', backgroundColor: '#f8fafc', borderTop: '1px solid #e2e8f0' }}>
          <div style={{ marginBottom: '16px' }}>
            <div style={{ fontSize: '12px', fontWeight: 700, color: '#64748b', textTransform: 'uppercase' }}>
              Selected Address Preview
            </div>
            <div style={{ fontSize: '14px', fontWeight: 700, color: '#0f172a', marginTop: '4px' }}>
              {isGeocoding ? 'Resolving address details...' : selectedAddress}
            </div>
            <div style={{ fontSize: '12px', color: '#166534', marginTop: '2px', fontFamily: 'monospace' }}>
              Lat: {position[0].toFixed(6)}, Lon: {position[1].toFixed(6)}
            </div>
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px' }}>
            <button
              onClick={onClose}
              style={{
                padding: '10px 20px',
                borderRadius: '10px',
                border: '1px solid #cbd5e1',
                backgroundColor: '#ffffff',
                color: '#475569',
                fontWeight: 700,
                fontSize: '13px',
                cursor: 'pointer'
              }}
            >
              Cancel
            </button>
            <button
              onClick={handleConfirm}
              style={{
                padding: '10px 24px',
                borderRadius: '10px',
                backgroundColor: '#166534',
                color: '#ffffff',
                border: 'none',
                fontWeight: 700,
                fontSize: '13px',
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                gap: '6px'
              }}
            >
              <Check size={16} />
              Confirm Selected Location
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
