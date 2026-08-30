import axiosClient from './axiosClient';

const MOCK_STATES = [
  'Tamil Nadu', 'Delhi', 'Maharashtra', 'Karnataka', 
  'Uttar Pradesh', 'West Bengal', 'Gujarat', 'Punjab', 'Kerala', 'Telangana', 'Andhra Pradesh'
];

const MOCK_DISTRICTS = {
  'Tamil Nadu': ['Chennai', 'Coimbatore', 'Madurai', 'Tiruchirappalli', 'Salem', 'Tirunelveli', 'Erode', 'Tiruppur', 'Vellore'],
  'Delhi': ['Central Delhi', 'New Delhi', 'South Delhi', 'North Delhi', 'East Delhi', 'West Delhi'],
  'Maharashtra': ['Mumbai Suburban', 'Mumbai City', 'Pune', 'Nagpur', 'Nashik', 'Thane'],
  'Karnataka': ['Bengaluru Urban', 'Bengaluru Rural', 'Mysuru', 'Hubballi-Dharwad', 'Dakshina Kannada'],
  'Uttar Pradesh': ['Lucknow', 'Kanpur', 'Varanasi', 'Agra', 'Gautam Buddha Nagar (Noida)', 'Ghaziabad'],
  'West Bengal': ['Kolkata', 'Howrah', 'North 24 Parganas', 'South 24 Parganas', 'Darjeeling'],
  'Gujarat': ['Ahmedabad', 'Surat', 'Vadodara', 'Rajkot'],
  'Punjab': ['Ludhiana', 'Amritsar', 'Jalandhar', 'Patiala'],
  'Kerala': ['Ernakulam', 'Thiruvananthapuram', 'Kozhikode'],
  'Telangana': ['Hyderabad', 'Rangareddy', 'Warangal'],
  'Andhra Pradesh': ['Visakhapatnam', 'Vijayawada', 'Guntur']
};

const MOCK_MARKETS = [
  // Tamil Nadu
  { id: 101, name: 'Koyambedu Wholesale Market Complex', district: 'Chennai', state: 'Tamil Nadu', type: 'APMC Mandi' },
  { id: 102, name: 'Mylapore Retail Hub', district: 'Chennai', state: 'Tamil Nadu', type: 'Local Retail Market' },
  { id: 103, name: 'Coimbatore Uzhavar Sandhai', district: 'Coimbatore', state: 'Tamil Nadu', type: 'Farmers Market' },
  { id: 104, name: 'Mettupalayam APMC Mandi', district: 'Coimbatore', state: 'Tamil Nadu', type: 'Wholesale Mandi' },
  { id: 105, name: 'Madurai Mattuthavani Wholesale Market', district: 'Madurai', state: 'Tamil Nadu', type: 'Wholesale Mandi' },
  { id: 106, name: 'Trichy Gandhi Market', district: 'Tiruchirappalli', state: 'Tamil Nadu', type: 'Central Market' },
  { id: 107, name: 'Salem Leigh Bazaar', district: 'Salem', state: 'Tamil Nadu', type: 'Agricultural Bazaar' },
  { id: 108, name: 'Tiruppur Central Market', district: 'Tiruppur', state: 'Tamil Nadu', type: 'City Market' },
  { id: 109, name: 'Erode Agricultural Co-Op Market', district: 'Erode', state: 'Tamil Nadu', type: 'Co-Op Market' },

  // Delhi NCR
  { id: 201, name: 'Azadpur APMC Mandi (Asia Largest)', district: 'Central Delhi', state: 'Delhi', type: 'Wholesale Mandi' },
  { id: 202, name: 'Okhla Wholesale Mandi', district: 'South Delhi', state: 'Delhi', type: 'Wholesale Mandi' },

  // Maharashtra
  { id: 301, name: 'Vashi APMC Market Complex', district: 'Mumbai Suburban', state: 'Maharashtra', type: 'APMC Market' },
  { id: 302, name: 'Dadar APMC Market', district: 'Mumbai City', state: 'Maharashtra', type: 'Retail & Wholesale' },
  { id: 303, name: 'Pune Marketyard Gultekdi', district: 'Pune', state: 'Maharashtra', type: 'APMC Yard' },

  // Karnataka
  { id: 401, name: 'KR Market (City Market)', district: 'Bengaluru Urban', state: 'Karnataka', type: 'Wholesale & Retail' },
  { id: 402, name: 'Yeshwanthpur APMC Yard', district: 'Bengaluru Urban', state: 'Karnataka', type: 'APMC Yard' },

  // Custom Store Entry
  { id: 999, name: '📍 Add / Type Custom Retail Shop or Kirana Store', district: 'Any', state: 'Any', type: 'Custom Entry' }
];

export const locationApi = {
  getStates: async () => {
    try {
      const response = await axiosClient.get('/api/v1/locations/states');
      if (response.data && response.data.data) {
        const raw = response.data.data;
        if (Array.isArray(raw) && raw.length > 0) {
          const list = raw.map(s => typeof s === 'string' ? s : (s.name || String(s)));
          return { success: true, data: Array.from(new Set([...list, ...MOCK_STATES])) };
        }
      }
      return { success: true, data: MOCK_STATES };
    } catch (error) {
      return { success: true, data: MOCK_STATES };
    }
  },

  getDistrictsByState: async (state) => {
    try {
      const response = await axiosClient.get(`/api/v1/locations/districts?state=${encodeURIComponent(state)}`);
      if (response.data && response.data.data) {
        const raw = response.data.data;
        if (Array.isArray(raw) && raw.length > 0) {
          const list = raw.map(d => typeof d === 'string' ? d : (d.name || String(d)));
          const defaultList = MOCK_DISTRICTS[state] || [];
          return { success: true, data: Array.from(new Set([...list, ...defaultList])) };
        }
      }
      return { success: true, data: MOCK_DISTRICTS[state] || ['Central District', 'North District', 'South District'] };
    } catch (error) {
      return { success: true, data: MOCK_DISTRICTS[state] || ['Central District', 'North District', 'South District'] };
    }
  },

  getMarkets: async (state = '', district = '') => {
    try {
      const response = await axiosClient.get('/api/v1/markets', { params: { state, district } });
      if (response.data && response.data.data) {
        const raw = response.data.data;
        if (Array.isArray(raw) && raw.length > 0) {
          return {
            success: true,
            data: raw.map(m => ({
              id: m.id || Math.floor(Math.random() * 1000),
              name: m.name || m,
              district: m.districtName || district || 'City',
              state: m.stateName || state || 'State',
              type: m.type || 'Retail Market'
            })).concat(MOCK_MARKETS[MOCK_MARKETS.length - 1])
          };
        }
      }

      // Filter MOCK_MARKETS
      let filtered = [...MOCK_MARKETS];
      if (state) {
        filtered = filtered.filter(m => m.state === state || m.id === 999);
      }
      if (district) {
        const distMatch = filtered.filter(m => m.district === district || m.id === 999);
        if (distMatch.length > 0) filtered = distMatch;
      }
      
      return { success: true, data: filtered.length > 0 ? filtered : MOCK_MARKETS };
    } catch (error) {
      let filtered = [...MOCK_MARKETS];
      if (state) filtered = filtered.filter(m => m.state === state || m.id === 999);
      if (district) {
        const distMatch = filtered.filter(m => m.district === district || m.id === 999);
        if (distMatch.length > 0) filtered = distMatch;
      }
      return { success: true, data: filtered.length > 0 ? filtered : MOCK_MARKETS };
    }
  }
};
