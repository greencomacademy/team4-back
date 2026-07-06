package com.deliveryinsider.domain.report.mapper;

import com.deliveryinsider.domain.report.projections.ReportOrderProjection;
import com.deliveryinsider.domain.report.projections.ReportSummaryProjection;
import com.deliveryinsider.domain.report.requests.ReportSearchRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.deliveryinsider.domain.report.projections.ReportPlatformProjection;
import java.util.List;
import com.deliveryinsider.domain.report.projections.ReportCancellationProjection;
import com.deliveryinsider.domain.report.projections.ReportCancellationSummaryProjection;

@Mapper
public interface ReportMapper {

    ReportSummaryProjection findSummary(
        @Param("storeId") Long storeId,
        @Param("search") ReportSearchRequest search
    );
    List<ReportOrderProjection> findOrders(
        @Param("storeId") Long storeId,
        @Param("search") ReportSearchRequest search
    );
    List<ReportPlatformProjection> findPlatforms(
        @Param("storeId") Long storeId,
        @Param("search") ReportSearchRequest search
    );
    List<ReportCancellationProjection> findCancellations(
        @Param("storeId") Long storeId,
        @Param("search") ReportSearchRequest search
    );

    List<ReportCancellationSummaryProjection> findCancellationSummary(
        @Param("storeId") Long storeId,
        @Param("search") ReportSearchRequest search
    );
}
