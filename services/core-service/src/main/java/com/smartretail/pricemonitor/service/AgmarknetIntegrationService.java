package com.smartretail.pricemonitor.service;

import com.smartretail.pricemonitor.dto.AgmarknetMarketDataDto;
import com.smartretail.pricemonitor.dto.AgmarknetSyncResponse;

import java.util.List;

public interface AgmarknetIntegrationService {

    AgmarknetSyncResponse seedNationwideMasterDataset();

    AgmarknetSyncResponse ingestAgmarknetData(List<AgmarknetMarketDataDto> records);

    AgmarknetSyncResponse syncFromLiveGovernmentApi(String apiKey);

    AgmarknetSyncResponse getSyncStatus();
}
