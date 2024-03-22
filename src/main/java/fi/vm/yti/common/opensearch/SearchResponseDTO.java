package fi.vm.yti.common.opensearch;

import java.util.List;

public class SearchResponseDTO<T extends IndexBase> {
    private long totalHitCount;
    private Integer pageSize;
    private Integer pageFrom;
    private List<T> responseObjects;

    public long getTotalHitCount() {
        return totalHitCount;
    }

    public void setTotalHitCount(long totalHitCount) {
        this.totalHitCount = totalHitCount;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getPageFrom() {
        return pageFrom;
    }

    public void setPageFrom(Integer pageFrom) {
        this.pageFrom = pageFrom;
    }

    public List<T> getResponseObjects() {
        return responseObjects;
    }

    public void setResponseObjects(List<T> responseObjects) {
        this.responseObjects = responseObjects;
    }
}
