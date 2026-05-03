package com.xianguoji.server.module.catalog.service;

import com.xianguoji.server.common.result.PageVO;
import com.xianguoji.server.module.catalog.dto.ProductQry;
import com.xianguoji.server.module.catalog.vo.*;

import java.util.List;

public interface CatalogService {

    List<CategoryTreeVO> getCategoryTree();

    List<BannerVO> getBannerList();

    List<String> getHotSearchList();

    PageVO<ProductVO> getProductPage(ProductQry qry);

    List<ProductVO> getRecommendProducts();

    ProductDetailVO getProductDetail(Long id);
}
