package com.inventory.controller;

import com.inventory.common.ApiResponse;
import com.inventory.dto.ProductDtos.ProductRequest;
import com.inventory.dto.ProductDtos.ProductResponse;
import com.inventory.dto.ProductDtos.ProductUpdateRequest;
import com.inventory.dto.ProductDtos.StockHistoryResponse;
import com.inventory.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Deliberately FEW endpoints - related features share one endpoint
 * wherever the underlying operation is really the same thing:
 *  - Search Product + Display Products  -> one GET (empty keyword = display all)
 *  - Update Stock + Edit Product        -> one PUT (pen icon)
 *
 * Every method here just validates the shape of the HTTP request and
 * calls into {@link ProductService}. No business rule, no collection,
 * no `if` about what's "valid" lives in this class.
 */
@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Add, edit, delete, search and track stock history for products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @Operation(summary = "Add Product", description = "Creates a new product. Code is validated as PRD-#### and must be unique.")
    public ResponseEntity<ApiResponse<ProductResponse>> addProduct(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(ApiResponse.success(productService.addProduct(request)));
    }

    @PutMapping("/{code}")
    @Operation(summary = "Edit Product / Update Stock", description = "Pen-icon action: edit name/category/price and/or adjust stock by a signed quantityChange.")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(@PathVariable String code,
                                                                       @RequestBody ProductUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(productService.updateProduct(code, request)));
    }

    @DeleteMapping("/{code}")
    @Operation(summary = "Delete Product", description = "Soft-deletes the product; the row and its stock history remain in Postgres.")
    public ResponseEntity<ApiResponse<ProductResponse>> deleteProduct(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.success(productService.deleteProduct(code)));
    }

    @GetMapping
    @Operation(summary = "Search / Display Products", description = "Leave keyword blank to display every active product.")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        List<ProductResponse> products = productService.searchProducts(keyword, category);
        Map<String, Object> meta = productService.getCatalogMeta();
        return ResponseEntity.ok(ApiResponse.success(products, meta));
    }

    @GetMapping("/{code}/history")
    @Operation(summary = "Stock History", description = "Eye-icon action: the full permanent stock log for one product code.")
    public ResponseEntity<ApiResponse<List<StockHistoryResponse>>> getStockHistory(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.success(productService.getStockHistory(code)));
    }
}
