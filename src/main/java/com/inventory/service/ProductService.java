package com.inventory.service;

import com.inventory.dto.ProductDtos.ProductRequest;
import com.inventory.dto.ProductDtos.ProductResponse;
import com.inventory.dto.ProductDtos.ProductUpdateRequest;
import com.inventory.dto.ProductDtos.StockHistoryResponse;

import java.util.List;
import java.util.Map;

/**
 * [Java basics: Abstraction, Interface, Polymorphism]
 * The controller only ever talks to this interface, never to
 * ProductServiceImpl directly - it doesn't need to know HOW products
 * are validated or stored, only WHAT operations are available. Spring
 * injects the concrete ProductServiceImpl at runtime (polymorphism:
 * the reference type is the interface, the actual object is the impl).
 */
public interface ProductService {

    ProductResponse addProduct(ProductRequest request);

    ProductResponse updateProduct(String code, ProductUpdateRequest request);

    ProductResponse deleteProduct(String code);

    List<ProductResponse> searchProducts(String keyword, String category);

    List<StockHistoryResponse> getStockHistory(String code);

    /** Small summary numbers (total, low-stock count, categories) for the UI's stat cards. */
    Map<String, Object> getCatalogMeta();
}
