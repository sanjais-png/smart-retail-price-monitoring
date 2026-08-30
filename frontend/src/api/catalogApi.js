import axiosClient from './axiosClient';

export const COMPREHENSIVE_COMMODITIES = [
  // 🟢 VEGETABLES (1-19)
  { id: 1, name: 'Tomato (Hybrid)', category: 'Vegetables', unit: 'kg', benchmarkPrice: 42.50, minPrice: 38.00, maxPrice: 48.00, priceChange: '+4.2%', yesterdayPrice: 40.80, status: 'MODERATE_SPIKE' },
  { id: 2, name: 'Onion (Red)', category: 'Vegetables', unit: 'kg', benchmarkPrice: 35.00, minPrice: 32.00, maxPrice: 40.00, priceChange: '-2.1%', yesterdayPrice: 35.75, status: 'STABLE' },
  { id: 3, name: 'Potato (Jyoti)', category: 'Vegetables', unit: 'kg', benchmarkPrice: 28.00, minPrice: 25.00, maxPrice: 32.00, priceChange: '+0.5%', yesterdayPrice: 27.86, status: 'STABLE' },
  { id: 11, name: 'Carrot (Ooty Organic)', category: 'Vegetables', unit: 'kg', benchmarkPrice: 48.00, minPrice: 42.00, maxPrice: 55.00, priceChange: '+3.1%', yesterdayPrice: 46.55, status: 'STABLE' },
  { id: 12, name: 'Cabbage (Green Harvest)', category: 'Vegetables', unit: 'kg', benchmarkPrice: 22.00, minPrice: 18.00, maxPrice: 26.00, priceChange: '-1.5%', yesterdayPrice: 22.33, status: 'STABLE' },
  { id: 13, name: 'Cauliflower (Fresh)', category: 'Vegetables', unit: 'kg', benchmarkPrice: 34.00, minPrice: 30.00, maxPrice: 40.00, priceChange: '+2.0%', yesterdayPrice: 33.33, status: 'STABLE' },
  { id: 14, name: 'Brinjal / Eggplant', category: 'Vegetables', unit: 'kg', benchmarkPrice: 26.00, minPrice: 22.00, maxPrice: 30.00, priceChange: '0.0%', yesterdayPrice: 26.00, status: 'STABLE' },
  { id: 15, name: 'Lady Finger / Bhindi', category: 'Vegetables', unit: 'kg', benchmarkPrice: 38.00, minPrice: 32.00, maxPrice: 44.00, priceChange: '+4.5%', yesterdayPrice: 36.36, status: 'MODERATE_SPIKE' },
  { id: 16, name: 'Green Chili (Spicy)', category: 'Vegetables', unit: 'kg', benchmarkPrice: 52.00, minPrice: 45.00, maxPrice: 60.00, priceChange: '+6.2%', yesterdayPrice: 48.96, status: 'HIGH_VOLATILITY' },
  { id: 17, name: 'Ginger (Fresh Organic)', category: 'Vegetables', unit: 'kg', benchmarkPrice: 110.00, minPrice: 95.00, maxPrice: 125.00, priceChange: '+1.8%', yesterdayPrice: 108.05, status: 'STABLE' },
  { id: 18, name: 'Garlic (Desi White)', category: 'Vegetables', unit: 'kg', benchmarkPrice: 180.00, minPrice: 160.00, maxPrice: 210.00, priceChange: '+7.5%', yesterdayPrice: 167.44, status: 'HIGH_VOLATILITY' },
  { id: 19, name: 'Cucumber (Green)', category: 'Vegetables', unit: 'kg', benchmarkPrice: 25.00, minPrice: 20.00, maxPrice: 30.00, priceChange: '-0.8%', yesterdayPrice: 25.20, status: 'STABLE' },
  { id: 20, name: 'Capsicum / Bell Pepper', category: 'Vegetables', unit: 'kg', benchmarkPrice: 65.00, minPrice: 55.00, maxPrice: 75.00, priceChange: '+2.5%', yesterdayPrice: 63.41, status: 'STABLE' },
  { id: 21, name: 'Spinach / Palak Bunch', category: 'Vegetables', unit: 'kg', benchmarkPrice: 30.00, minPrice: 24.00, maxPrice: 36.00, priceChange: '+1.2%', yesterdayPrice: 29.64, status: 'STABLE' },
  { id: 22, name: 'Bitter Gourd / Karela', category: 'Vegetables', unit: 'kg', benchmarkPrice: 45.00, minPrice: 38.00, maxPrice: 52.00, priceChange: '+3.0%', yesterdayPrice: 43.68, status: 'STABLE' },
  { id: 23, name: 'Bottle Gourd / Lauki', category: 'Vegetables', unit: 'kg', benchmarkPrice: 24.00, minPrice: 20.00, maxPrice: 28.00, priceChange: '-2.0%', yesterdayPrice: 24.49, status: 'STABLE' },
  { id: 24, name: 'Radish / Mooli', category: 'Vegetables', unit: 'kg', benchmarkPrice: 28.00, minPrice: 22.00, maxPrice: 34.00, priceChange: '0.0%', yesterdayPrice: 28.00, status: 'STABLE' },
  { id: 25, name: 'Sweet Potato', category: 'Vegetables', unit: 'kg', benchmarkPrice: 36.00, minPrice: 30.00, maxPrice: 42.00, priceChange: '+1.5%', yesterdayPrice: 35.46, status: 'STABLE' },
  { id: 26, name: 'Drumstick / Murungakkai', category: 'Vegetables', unit: 'kg', benchmarkPrice: 75.00, minPrice: 60.00, maxPrice: 90.00, priceChange: '+8.7%', yesterdayPrice: 69.00, status: 'HIGH_VOLATILITY' },

  // 🌾 GRAINS & CEREALS (4-5, 27-32)
  { id: 4, name: 'Rice (Basmati 1121)', category: 'Grains', unit: 'kg', benchmarkPrice: 85.00, minPrice: 80.00, maxPrice: 95.00, priceChange: '+1.5%', yesterdayPrice: 83.74, status: 'STABLE' },
  { id: 5, name: 'Wheat (Sharbati Premium)', category: 'Grains', unit: 'kg', benchmarkPrice: 32.00, minPrice: 30.00, maxPrice: 36.00, priceChange: '+0.8%', yesterdayPrice: 31.75, status: 'STABLE' },
  { id: 27, name: 'Sona Masoori Rice', category: 'Grains', unit: 'kg', benchmarkPrice: 56.00, minPrice: 50.00, maxPrice: 62.00, priceChange: '+0.5%', yesterdayPrice: 55.72, status: 'STABLE' },
  { id: 28, name: 'Ponni Boiled Rice', category: 'Grains', unit: 'kg', benchmarkPrice: 52.00, minPrice: 48.00, maxPrice: 58.00, priceChange: '0.0%', yesterdayPrice: 52.00, status: 'STABLE' },
  { id: 29, name: 'Whole Wheat Flour (Atta)', category: 'Grains', unit: 'kg', benchmarkPrice: 38.00, minPrice: 35.00, maxPrice: 42.00, priceChange: '+1.0%', yesterdayPrice: 37.62, status: 'STABLE' },
  { id: 30, name: 'Ragi (Finger Millet)', category: 'Grains', unit: 'kg', benchmarkPrice: 45.00, minPrice: 40.00, maxPrice: 50.00, priceChange: '+2.2%', yesterdayPrice: 44.03, status: 'STABLE' },
  { id: 31, name: 'Jowar (Sorghum Whole)', category: 'Grains', unit: 'kg', benchmarkPrice: 42.00, minPrice: 38.00, maxPrice: 48.00, priceChange: '+0.5%', yesterdayPrice: 41.79, status: 'STABLE' },
  { id: 32, name: 'Bajra (Pearl Millet)', category: 'Grains', unit: 'kg', benchmarkPrice: 34.00, minPrice: 30.00, maxPrice: 38.00, priceChange: '-1.0%', yesterdayPrice: 34.34, status: 'STABLE' },

  // 🥣 PULSES & DAL (6, 33-38)
  { id: 6, name: 'Toor Dal (Arhar Premium)', category: 'Pulses', unit: 'kg', benchmarkPrice: 145.00, minPrice: 138.00, maxPrice: 155.00, priceChange: '+5.4%', yesterdayPrice: 137.57, status: 'HIGH_VOLATILITY' },
  { id: 33, name: 'Moong Dal (Yellow Split)', category: 'Pulses', unit: 'kg', benchmarkPrice: 115.00, minPrice: 105.00, maxPrice: 125.00, priceChange: '+1.8%', yesterdayPrice: 112.96, status: 'STABLE' },
  { id: 34, name: 'Chana Dal (Bengal Gram)', category: 'Pulses', unit: 'kg', benchmarkPrice: 82.00, minPrice: 75.00, maxPrice: 90.00, priceChange: '+2.5%', yesterdayPrice: 80.00, status: 'STABLE' },
  { id: 35, name: 'Urad Dal (Black Whole/Split)', category: 'Pulses', unit: 'kg', benchmarkPrice: 128.00, minPrice: 120.00, maxPrice: 138.00, priceChange: '+3.2%', yesterdayPrice: 124.03, status: 'STABLE' },
  { id: 36, name: 'Masoor Dal (Red Lentil)', category: 'Pulses', unit: 'kg', benchmarkPrice: 92.00, minPrice: 85.00, maxPrice: 100.00, priceChange: '0.0%', yesterdayPrice: 92.00, status: 'STABLE' },
  { id: 37, name: 'Rajma (Chitra Red Beans)', category: 'Pulses', unit: 'kg', benchmarkPrice: 138.00, minPrice: 125.00, maxPrice: 150.00, priceChange: '+4.0%', yesterdayPrice: 132.69, status: 'MODERATE_SPIKE' },
  { id: 38, name: 'Kabuli Chana (Chickpeas)', category: 'Pulses', unit: 'kg', benchmarkPrice: 125.00, minPrice: 115.00, maxPrice: 138.00, priceChange: '+1.5%', yesterdayPrice: 123.15, status: 'STABLE' },

  // 🛢️ EDIBLE OILS & GHEE (7, 39-43)
  { id: 7, name: 'Mustard Oil (Kachi Ghani)', category: 'Edible Oils', unit: 'Litre', benchmarkPrice: 135.00, minPrice: 128.00, maxPrice: 142.00, priceChange: '-1.2%', yesterdayPrice: 136.64, status: 'STABLE' },
  { id: 39, name: 'Refined Sunflower Oil', category: 'Edible Oils', unit: 'Litre', benchmarkPrice: 128.00, minPrice: 120.00, maxPrice: 138.00, priceChange: '0.0%', yesterdayPrice: 128.00, status: 'STABLE' },
  { id: 40, name: 'Groundnut Oil (Pure)', category: 'Edible Oils', unit: 'Litre', benchmarkPrice: 175.00, minPrice: 165.00, maxPrice: 188.00, priceChange: '+2.1%', yesterdayPrice: 171.40, status: 'STABLE' },
  { id: 41, name: 'Coconut Oil (Filtered)', category: 'Edible Oils', unit: 'Litre', benchmarkPrice: 210.00, minPrice: 195.00, maxPrice: 230.00, priceChange: '+3.5%', yesterdayPrice: 202.89, status: 'STABLE' },
  { id: 42, name: 'Pure Cow Ghee', category: 'Edible Oils', unit: 'Litre', benchmarkPrice: 580.00, minPrice: 550.00, maxPrice: 620.00, priceChange: '+1.2%', yesterdayPrice: 573.12, status: 'STABLE' },

  // 🥛 DAIRY & EGGS (8, 43-46)
  { id: 8, name: 'Milk (Full Cream Pasteurized)', category: 'Dairy', unit: 'Litre', benchmarkPrice: 66.00, minPrice: 64.00, maxPrice: 68.00, priceChange: '0.0%', yesterdayPrice: 66.00, status: 'STABLE' },
  { id: 43, name: 'Milk (Toned)', category: 'Dairy', unit: 'Litre', benchmarkPrice: 54.00, minPrice: 52.00, maxPrice: 56.00, priceChange: '0.0%', yesterdayPrice: 54.00, status: 'STABLE' },
  { id: 44, name: 'Fresh Paneer (Cottage Cheese)', category: 'Dairy', unit: 'kg', benchmarkPrice: 360.00, minPrice: 340.00, maxPrice: 390.00, priceChange: '+2.0%', yesterdayPrice: 352.94, status: 'STABLE' },
  { id: 45, name: 'Farm Fresh Eggs (12 Pack)', category: 'Dairy', unit: 'Pack', benchmarkPrice: 84.00, minPrice: 76.00, maxPrice: 90.00, priceChange: '+3.8%', yesterdayPrice: 80.92, status: 'STABLE' },

  // 🍎 FRUITS (10, 47-50)
  { id: 10, name: 'Apple (Kinnaur/Kashmir)', category: 'Fruits', unit: 'kg', benchmarkPrice: 120.00, minPrice: 110.00, maxPrice: 140.00, priceChange: '+8.3%', yesterdayPrice: 110.80, status: 'HIGH_VOLATILITY' },
  { id: 47, name: 'Banana (Robusta Premium)', category: 'Fruits', unit: 'Dozen', benchmarkPrice: 48.00, minPrice: 40.00, maxPrice: 55.00, priceChange: '+1.5%', yesterdayPrice: 47.29, status: 'STABLE' },
  { id: 48, name: 'Sweet Orange / Mosambi', category: 'Fruits', unit: 'kg', benchmarkPrice: 65.00, minPrice: 55.00, maxPrice: 75.00, priceChange: '-2.5%', yesterdayPrice: 66.66, status: 'STABLE' },
  { id: 49, name: 'Pomegranate (Kandhari)', category: 'Fruits', unit: 'kg', benchmarkPrice: 160.00, minPrice: 140.00, maxPrice: 185.00, priceChange: '+4.2%', yesterdayPrice: 153.55, status: 'MODERATE_SPIKE' },

  // 🍬 ESSENTIALS & HOUSEHOLD (9, 51-54)
  { id: 9, name: 'Sugar (M-30 Fine Granules)', category: 'Essentials', unit: 'kg', benchmarkPrice: 44.00, minPrice: 42.00, maxPrice: 46.00, priceChange: '+0.5%', yesterdayPrice: 43.78, status: 'STABLE' },
  { id: 51, name: 'Organic Jaggery (Gud)', category: 'Essentials', unit: 'kg', benchmarkPrice: 58.00, minPrice: 52.00, maxPrice: 65.00, priceChange: '+1.0%', yesterdayPrice: 57.42, status: 'STABLE' },
  { id: 52, name: 'Assam CTC Leaf Tea', category: 'Essentials', unit: 'kg', benchmarkPrice: 280.00, minPrice: 250.00, maxPrice: 320.00, priceChange: '+0.8%', yesterdayPrice: 277.77, status: 'STABLE' },
  { id: 53, name: 'Iodized Crystal Salt', category: 'Essentials', unit: 'kg', benchmarkPrice: 22.00, minPrice: 20.00, maxPrice: 24.00, priceChange: '0.0%', yesterdayPrice: 22.00, status: 'STABLE' },
  { id: 54, name: 'Turmeric Powder (Haldi)', category: 'Essentials', unit: 'kg', benchmarkPrice: 160.00, minPrice: 145.00, maxPrice: 180.00, priceChange: '+2.5%', yesterdayPrice: 156.09, status: 'STABLE' }
];

export const formatCommodityRecord = (item) => {
  if (!item) return null;

  // Match with comprehensive reference list if raw DB record lacks catalog metrics
  const matched = COMPREHENSIVE_COMMODITIES.find(
    c => String(c.id) === String(item.id) ||
         (c.name && item.name && c.name.toLowerCase().trim() === item.name.toLowerCase().trim())
  );

  const benchmarkPrice = item.benchmarkPrice ?? item.baseBenchmarkPrice ?? item.price ?? matched?.benchmarkPrice ?? null;
  const unit = item.unit || matched?.unit || 'kg';
  const yesterdayPrice = item.yesterdayPrice ?? matched?.yesterdayPrice ?? null;
  const priceChange = item.priceChange ?? matched?.priceChange ?? null;
  const minPrice = item.minPrice ?? matched?.minPrice ?? null;
  const maxPrice = item.maxPrice ?? matched?.maxPrice ?? null;
  const status = item.status || matched?.status || 'STABLE';

  return {
    id: item.id ?? matched?.id,
    name: item.name || matched?.name || 'Unknown Commodity',
    code: item.code || matched?.code || '',
    description: item.description || matched?.description || '',
    category: item.categoryName || item.category || matched?.category || 'Essential',
    unit,
    benchmarkPrice,
    yesterdayPrice,
    priceChange,
    minPrice,
    maxPrice,
    status
  };
};

export const catalogApi = {
  getCategories: async () => {
    try {
      const response = await axiosClient.get('/api/v1/categories');
      if (response.data && response.data.data) {
        const cats = response.data.data;
        if (Array.isArray(cats) && cats.length > 0) {
          return {
            success: true,
            data: cats.map(c => typeof c === 'string' ? c : (c.name || c))
          };
        }
      }
      return {
        success: true,
        data: ['Vegetables', 'Grains', 'Pulses', 'Edible Oils', 'Dairy', 'Essentials', 'Fruits']
      };
    } catch (error) {
      return {
        success: true,
        data: ['Vegetables', 'Grains', 'Pulses', 'Edible Oils', 'Dairy', 'Essentials', 'Fruits']
      };
    }
  },

  getCommodities: async (category = null, search = '') => {
    try {
      const params = { size: 500 };
      if (search) params.query = search;

      const response = await axiosClient.get('/api/v1/commodities', { params });
      
      let list = [];
      if (response.data && response.data.data) {
        const rawData = response.data.data;
        if (Array.isArray(rawData)) {
          list = rawData;
        } else if (rawData.content && Array.isArray(rawData.content)) {
          list = rawData.content;
        }
      }

      const formatted = list.map((item) => formatCommodityRecord(item));

      // Merge backend items with COMPREHENSIVE_COMMODITIES to guarantee all catalog items are populated
      const combined = [...formatted];
      for (const comp of COMPREHENSIVE_COMMODITIES) {
        if (!combined.some(c => String(c.id) === String(comp.id) || c.name.toLowerCase() === comp.name.toLowerCase())) {
          combined.push(comp);
        }
      }

      let filtered = combined;
      if (category && category !== 'All') {
        filtered = filtered.filter(c => c.category && c.category.toLowerCase() === category.toLowerCase());
      }
      if (search) {
        filtered = filtered.filter(c => c.name && c.name.toLowerCase().includes(search.toLowerCase()));
      }
      return { success: true, data: filtered };
    } catch (error) {
      let filtered = [...COMPREHENSIVE_COMMODITIES];
      if (category && category !== 'All') {
        filtered = filtered.filter(c => c.category && c.category.toLowerCase() === category.toLowerCase());
      }
      if (search) {
        filtered = filtered.filter(c => c.name && c.name.toLowerCase().includes(search.toLowerCase()));
      }
      return {
        success: true,
        data: filtered
      };
    }
  },

  getCommodityById: async (id) => {
    if (!id) return { success: false, data: null, error: 'Invalid commodity ID' };

    try {
      const response = await axiosClient.get(`/api/v1/commodities/${id}`);
      if (response.data && response.data.data) {
        const normalized = formatCommodityRecord(response.data.data);
        return { success: true, data: normalized };
      }
    } catch (error) {
      // API call failed, fallback to checking COMPREHENSIVE_COMMODITIES by exact ID or name
    }

    const found = COMPREHENSIVE_COMMODITIES.find(
      c => String(c.id) === String(id) || c.name.toLowerCase().includes(String(id).toLowerCase())
    );

    if (found) {
      return { success: true, data: formatCommodityRecord(found) };
    }

    return { success: false, data: null, error: 'Commodity not found' };
  }
};
