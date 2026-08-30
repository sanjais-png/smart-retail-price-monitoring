import axiosClient from './axiosClient';

const CITY_STATE_LOOKUP = {
  'hyderabad': { city: 'Hyderabad', state: 'Telangana' },
  'secunderabad': { city: 'Secunderabad', state: 'Telangana' },
  'bangalore': { city: 'Bangalore', state: 'Karnataka' },
  'bengaluru': { city: 'Bengaluru', state: 'Karnataka' },
  'chennai': { city: 'Chennai', state: 'Tamil Nadu' },
  'coimbatore': { city: 'Coimbatore', state: 'Tamil Nadu' },
  'madurai': { city: 'Madurai', state: 'Tamil Nadu' },
  'trichy': { city: 'Trichy', state: 'Tamil Nadu' },
  'salem': { city: 'Salem', state: 'Tamil Nadu' },
  'mumbai': { city: 'Mumbai', state: 'Maharashtra' },
  'pune': { city: 'Pune', state: 'Maharashtra' },
  'nagpur': { city: 'Nagpur', state: 'Maharashtra' },
  'delhi': { city: 'New Delhi', state: 'Delhi' },
  'new delhi': { city: 'New Delhi', state: 'Delhi' }
};

export const governanceApi = {
  sendAiChatQuery: async (query, activeLocation, activeState, history = [], mode = 'AI') => {
    try {
      const response = await axiosClient.post('/api/v1/ai/chat', {
        query,
        mode,
        activeLocation,
        activeState,
        history: history.map(h => ({ sender: h.sender, text: h.text }))
      });

      if (response.data && response.data.data) {
        return response.data.data;
      }
      return { reply: 'AI response generated successfully', source: mode };
    } catch (e) {
      console.error('API sendAiChatQuery failed:', e);
      return {
        reply: e.response?.data?.message || 'AI assistance is temporarily unavailable due to current usage limits. Please try again later.',
        source: 'UNAVAILABLE',
        groundingStatus: 'AI_UNAVAILABLE'
      };
    }
  },

  submitComplaint: async (complaintData) => {
    try {
      const response = await axiosClient.post('/api/v1/complaints', {
        marketId: complaintData.marketId !== undefined ? complaintData.marketId : null,
        commodityId: complaintData.commodityId !== undefined ? complaintData.commodityId : null,
        title: complaintData.title,
        description: complaintData.description
      });

      if (response.data && response.data.data) {
        const serverItem = response.data.data;
        const localSaved = JSON.parse(localStorage.getItem('smart_retail_my_complaints') || '[]');
        localSaved.unshift(serverItem);
        localStorage.setItem('smart_retail_my_complaints', JSON.stringify(localSaved));
      }

      return response;
    } catch (e) {
      console.error('API submitComplaint failed:', e);
      throw e;
    }
  },

  getMyComplaints: async () => {
    try {
      const response = await axiosClient.get('/api/v1/complaints/my');
      if (response.data && response.data.data) {
        const raw = response.data.data.content || response.data.data;
        if (Array.isArray(raw)) {
          const mapped = raw.map(c => {
            const desc = c.description || '';
            const comMatch = desc.match(/- Commodity:\s*([^\n]+)/i);
            const storeMatch = desc.match(/- Store Name:\s*([^\n]+)/i);
            const pricePaidMatch = desc.match(/Price Paid:\s*₹?\s*([\d,.]+)/i);
            const benchMatch = desc.match(/Mandi Fair Benchmark:\s*₹?\s*([\d,.]+)/i);
            const excessMatch = desc.match(/Excess:\s*\+?([\d.]+)%/i);
            const billMatch = desc.match(/Bill Invoice No:\s*([^\n]+)/i);

            const storeNameExtracted = storeMatch ? storeMatch[1].trim() : null;
            const isPlaceholderStore = storeNameExtracted && (
              storeNameExtracted.includes('Add / Type Custom Retail Shop') ||
              storeNameExtracted.includes('Custom Local Kirana Shop')
            );

            const comName = (comMatch && comMatch[1].trim()) ? comMatch[1].trim() : (c.commodityName || c.commodity || 'Not Available');
            const mktName = (storeNameExtracted && !isPlaceholderStore)
              ? storeNameExtracted
              : ((c.marketName && c.marketName.toLowerCase() !== 'mandhi hub')
                  ? c.marketName
                  : (storeNameExtracted || c.marketName || c.market || 'Not Available'));

            const reportedPrice = pricePaidMatch ? parseFloat(pricePaidMatch[1].replace(/,/g, '')) : (c.reportedPrice || 'Not Available');
            const fairPrice = benchMatch ? parseFloat(benchMatch[1].replace(/,/g, '')) : (c.fairPrice || 'Not Available');
            const inflationPercent = excessMatch ? `+${excessMatch[1]}%` : (c.inflationPercent || 'Not Available');
            const billNumber = billMatch ? billMatch[1].trim() : (c.billNumber || 'Not Available');

            const formattedId = c.id ? (String(c.id).startsWith('CMP-') ? String(c.id) : `CMP-${c.id}`) : 'CMP-101';

            return {
              id: formattedId,
              displayId: formattedId,
              rawId: c.id,
              date: c.createdAt ? c.createdAt.split('T')[0] : new Date().toISOString().split('T')[0],
              commodity: comName,
              commodityName: comName,
              market: mktName,
              marketName: mktName,
              title: c.title || `Price Gouging Dispute on ${comName}`,
              description: c.description,
              reportedPrice: reportedPrice,
              fairPrice: fairPrice,
              inflationPercent: inflationPercent,
              billNumber: billNumber,
              user: c.email || c.username || 'Not Available',
              status: c.status || 'PENDING',
              resolutionNotes: c.resolutionNotes || '',
              createdAt: c.createdAt
            };
          });
          return { success: true, data: mapped };
        }
      }
      return response;
    } catch (e) {
      console.error('Failed to fetch user complaints:', e);
      return { success: false, error: e?.response?.data?.message || e.message, data: [] };
    }
  },

  getComplaints: async () => {
    try {
      let response;
      try {
        response = await axiosClient.get('/api/v1/complaints');
      } catch (err) {
        response = await axiosClient.get('/api/v1/complaints/my');
      }

      if (response.data && response.data.data) {
        const raw = response.data.data.content || response.data.data;
        if (Array.isArray(raw)) {
          const mapped = raw.map(c => {
            const desc = c.description || '';
            const comMatch = desc.match(/- Commodity:\s*([^\n]+)/i);
            const storeMatch = desc.match(/- Store Name:\s*([^\n]+)/i);
            const pricePaidMatch = desc.match(/Price Paid:\s*₹?\s*([\d,.]+)/i);
            const benchMatch = desc.match(/Mandi Fair Benchmark:\s*₹?\s*([\d,.]+)/i);
            const excessMatch = desc.match(/Excess:\s*\+?([\d.]+)%/i);
            const billMatch = desc.match(/Bill Invoice No:\s*([^\n]+)/i);

            const storeNameExtracted = storeMatch ? storeMatch[1].trim() : null;
            const isPlaceholderStore = storeNameExtracted && (
              storeNameExtracted.includes('Add / Type Custom Retail Shop') ||
              storeNameExtracted.includes('Custom Local Kirana Shop')
            );

            const comName = (comMatch && comMatch[1].trim()) ? comMatch[1].trim() : (c.commodityName || c.commodity || 'Not Available');
            const mktName = (storeNameExtracted && !isPlaceholderStore)
              ? storeNameExtracted
              : ((c.marketName && c.marketName.toLowerCase() !== 'mandhi hub')
                  ? c.marketName
                  : (storeNameExtracted || c.marketName || c.market || 'Not Available'));

            const reportedPrice = pricePaidMatch ? parseFloat(pricePaidMatch[1].replace(/,/g, '')) : (c.reportedPrice || 'Not Available');
            const fairPrice = benchMatch ? parseFloat(benchMatch[1].replace(/,/g, '')) : (c.fairPrice || 'Not Available');
            const inflationPercent = excessMatch ? `+${excessMatch[1]}%` : (c.inflationPercent || 'Not Available');
            const billNumber = billMatch ? billMatch[1].trim() : (c.billNumber || 'Not Available');

            const formattedId = c.id ? (String(c.id).startsWith('CMP-') ? String(c.id) : `CMP-${c.id}`) : 'CMP-101';

            return {
              id: formattedId,
              rawId: c.id,
              date: c.createdAt ? c.createdAt.split('T')[0] : new Date().toISOString().split('T')[0],
              commodity: comName,
              market: mktName,
              title: c.title || `Price Gouging Dispute on ${comName}`,
              description: c.description || 'Excess retail price charged over mandi fair benchmark.',
              reportedPrice: reportedPrice,
              fairPrice: fairPrice,
              inflationPercent: inflationPercent,
              billNumber: billNumber,
              user: c.email || c.username || 'Not Available',
              status: c.status || 'PENDING',
              resolutionNotes: c.resolutionNotes || ''
            };
          });

          return { success: true, data: mapped };
        }
      }

      return { success: true, data: [] };
    } catch (e) {
      console.error('Failed to fetch complaints:', e);
      return { success: false, error: e?.response?.data?.message || e.message, data: [] };
    }
  },

  updateComplaintStatus: async (id, status, notes = '') => {
    try {
      const cleanId = String(id).replace('CMP-', '');
      const response = await axiosClient.patch(`/api/v1/complaints/${cleanId}/status`, null, {
        params: { status, resolutionNotes: notes }
      });
      return response;
    } catch (e) {
      console.error('API updateComplaintStatus failed:', e);
      throw e;
    }
  },

  addMarket: async (marketData) => {
    try {
      const cleanCity = (marketData.city || '').trim().toLowerCase();
      const lookup = CITY_STATE_LOOKUP[cleanCity] || {
        city: marketData.city || 'Central Zone',
        state: marketData.state || 'Tamil Nadu'
      };

      const response = await axiosClient.post('/api/v1/location/markets', {
        name: marketData.name,
        code: `MKT-${Date.now().toString().slice(-6)}`,
        city: lookup.city,
        stateName: lookup.state,
        districtName: lookup.city,
        address: marketData.address || `${lookup.city}, ${lookup.state}`,
        isApmcRegistered: true
      });
      return response;
    } catch (e) {
      console.error('API addMarket failed:', e);
      throw e;
    }
  }
};
