package com.smartretail.pricemonitor.service;

import com.smartretail.pricemonitor.dto.*;

import java.util.List;

public interface DatasetService {

    List<MarketTrendDto> getMarketTrendsDataset(Long commodityId, Long marketId, String periodType);

    List<RegionalBenchmarkDto> getRegionalPriceBenchmarks(String state, String city);

    List<DatasetExportRecord> getCompleteExportDataset(Long commodityId, String state);

    String exportDatasetAsCsv(Long commodityId, String state);

    int bulkIngestDataset(List<BulkDatasetIngestDto> datasetEntries);
}
