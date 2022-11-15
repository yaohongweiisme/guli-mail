package com.wei.gulimall.search.service;

import com.wei.common.to.es.EsSkuModel;
import java.io.IOException;
import java.util.List;

public interface ProductUpService {

    public boolean StatusUp(List<EsSkuModel> esSkuModels) throws IOException;

}
