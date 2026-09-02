package com.inventory.service;

import com.inventory.common.AppConstants;
import com.inventory.dto.ProductDtos.ProductRequest;
import com.inventory.dto.ProductDtos.ProductResponse;
import com.inventory.dto.ProductDtos.ProductUpdateRequest;
import com.inventory.dto.ProductDtos.StockHistoryResponse;
import com.inventory.entity.Product;
import com.inventory.entity.StockAction;
import com.inventory.entity.StockHistory;
import com.inventory.exception.AppExceptions.DuplicateProductCodeException;
import com.inventory.exception.AppExceptions.InvalidProductCodeException;
import com.inventory.exception.AppExceptions.ProductNotFoundException;
import com.inventory.exception.AppExceptions.ValidationFailedException;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.StockHistoryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * ALL product business logic lives here on purpose - implements
 * {@link AppConstants} so its regex/threshold constants are used
 * directly (an interface's constants, used by an implementing class).
 * ProductController never contains an `if`, a validation rule, or a
 * collection - it only calls methods on this class.
 */
@Service
public class ProductServiceImpl implements ProductService, AppConstants {

    // Data Types: a compiled Pattern (reference type) built once from
    // the interface's String constant.
    private static final Pattern CODE_PATTERN = Pattern.compile(PRODUCT_CODE_REGEX);

    private final ProductRepository productRepository;
    private final StockHistoryRepository stockHistoryRepository;

    // [Java basics: Constructor, Encapsulation]
    // Constructor injection - Spring hands us the repositories once,
    // they're stored as private final fields nothing else can touch.
    public ProductServiceImpl(ProductRepository productRepository,
                               StockHistoryRepository stockHistoryRepository) {
        this.productRepository = productRepository;
        this.stockHistoryRepository = stockHistoryRepository;
    }

    // ---------------------------------------------------------------
    // Product Code Validation
    // ---------------------------------------------------------------
    private void validateProductCode(String code) {
        // If, Else + logical Operators (||, &&)
        if (code == null || !CODE_PATTERN.matcher(code).matches()) {
            throw new InvalidProductCodeException("Invalid product code. " + PRODUCT_CODE_HINT);
        }
        if (productRepository.existsByCode(code)) {
            throw new DuplicateProductCodeException(code);
        }
    }

    // ---------------------------------------------------------------
    // Add Product
    // ---------------------------------------------------------------
    @Override
    public ProductResponse addProduct(ProductRequest request) {
        String code = request.getCode() == null ? null : request.getCode().trim().toUpperCase();
        validateProductCode(code);

        if (request.getName() == null || request.getName().isBlank()) {
            throw new ValidationFailedException("Product name is required");
        }
        // Operators: relational comparisons
        if (request.getPrice() < 0 || request.getQuantity() < 0) {
            throw new ValidationFailedException("Price and quantity cannot be negative");
        }

        Product product = new Product(code, request.getName().trim(), request.getCategory().trim(),
                request.getPrice(), request.getQuantity());
        productRepository.save(product);

        logHistory(product, StockAction.CREATED, 0, product.getQuantity(),
                "Product created with initial stock of " + product.getQuantity());

        return new ProductResponse(product);
    }

    // ---------------------------------------------------------------
    // Update Stock + Edit Product (single endpoint behind the pen icon)
    // ---------------------------------------------------------------
    @Override
    public ProductResponse updateProduct(String code, ProductUpdateRequest request) {
        Product product = findActiveOrThrow(code);
        int previousQuantity = product.getQuantity();
        boolean detailsChanged = false;

        // Loops/Methods: small guarded updates, each is its own "if".
        if (request.getName() != null && !request.getName().isBlank()) {
            product.setName(request.getName().trim());
            detailsChanged = true;
        }
        if (request.getCategory() != null && !request.getCategory().isBlank()) {
            product.setCategory(request.getCategory().trim());
            detailsChanged = true;
        }
        if (request.getPrice() != null) {
            if (request.getPrice() < 0) {
                throw new ValidationFailedException("Price cannot be negative");
            }
            product.setPrice(request.getPrice());
            detailsChanged = true;
        }

        boolean stockChanged = request.getQuantityChange() != null && request.getQuantityChange() != 0;
        if (stockChanged) {
            int newQuantity = product.getQuantity() + request.getQuantityChange(); // Operators: +
            if (newQuantity < 0) {
                throw new ValidationFailedException("Stock cannot go below zero for " + code);
            }
            product.setQuantity(newQuantity);
        }

        if (!detailsChanged && !stockChanged) {
            throw new ValidationFailedException("Nothing to update - provide at least one field");
        }

        productRepository.save(product);

        // Switch on the enum to write a clear history note depending on
        // what kind of change this was.
        StockAction action;
        String note;
        if (stockChanged) {
            action = request.getQuantityChange() > 0 ? StockAction.STOCK_INCREASED : StockAction.STOCK_DECREASED;
            note = (request.getReason() == null || request.getReason().isBlank())
                    ? "Stock adjusted by " + request.getQuantityChange()
                    : request.getReason().trim();
        } else {
            action = StockAction.DETAILS_UPDATED;
            note = "Product details edited";
        }
        logHistory(product, action, previousQuantity, product.getQuantity(), note);

        return new ProductResponse(product);
    }

    // ---------------------------------------------------------------
    // Delete Product (soft delete - row + history stay in Postgres)
    // ---------------------------------------------------------------
    @Override
    public ProductResponse deleteProduct(String code) {
        Product product = findActiveOrThrow(code);
        product.setDeleted(true);
        product.setDeletedAt(LocalDateTime.now());
        productRepository.save(product);

        logHistory(product, StockAction.DELETED, product.getQuantity(), product.getQuantity(),
                "Product removed from the active catalog (record retained)");

        return new ProductResponse(product);
    }

    // ---------------------------------------------------------------
    // Search Product + Display Products (one method, empty keyword = display all)
    // ---------------------------------------------------------------
    @Override
    public List<ProductResponse> searchProducts(String keyword, String category) {
        String cleanKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        String cleanCategory = (category == null || category.isBlank()) ? null : category.trim();

        List<Product> found = productRepository.search(cleanKeyword, cleanCategory);

        // ArrayList: simple ordered collection to hand back over the API.
        List<ProductResponse> results = new ArrayList<>();
        for (Product p : found) { // Loops: for-each
            results.add(new ProductResponse(p));
        }
        return results;
    }

    // ---------------------------------------------------------------
    // Stock History
    // ---------------------------------------------------------------
    @Override
    public List<StockHistoryResponse> getStockHistory(String code) {
        String normalized = code.trim().toUpperCase();
        // Look up including soft-deleted products - history must still
        // be viewable for a product that was later deleted.
        productRepository.findByCode(normalized).orElseThrow(() -> new ProductNotFoundException(normalized));

        List<StockHistory> history = stockHistoryRepository.findByProductCodeOrderByCreatedAtDesc(normalized);
        List<StockHistoryResponse> results = new ArrayList<>();
        for (StockHistory h : history) {
            results.add(new StockHistoryResponse(h));
        }
        return results;
    }

    // ---------------------------------------------------------------
    // Catalog-wide summary used by the Products page stat cards
    // ---------------------------------------------------------------
    @Override
    public Map<String, Object> getCatalogMeta() {
        List<Product> active = productRepository.search(null, null);

        // TreeSet: unique category names, always alphabetically sorted -
        // perfect for a filter dropdown.
        TreeSet<String> categories = new TreeSet<>();
        // TreeMap: category -> total quantity in that category, kept
        // sorted by category name for a tidy "stock by category" report.
        TreeMap<String, Integer> stockByCategory = new TreeMap<>();

        int lowStockCount = 0;
        for (Product p : active) {
            categories.add(p.getCategory());
            stockByCategory.merge(p.getCategory(), p.getQuantity(), Integer::sum);
            if (p.getQuantity() < LOW_STOCK_THRESHOLD) { // Operators: <
                lowStockCount++;
            }
        }

        Map<String, Object> meta = new java.util.LinkedHashMap<>();
        meta.put("totalProducts", active.size());
        meta.put("lowStockCount", lowStockCount);
        meta.put("categories", categories); // Arrays-like ordered unique set, serialized as a JSON array
        meta.put("stockByCategory", stockByCategory);
        return meta;
    }

    // ---------------------------------------------------------------
    // Shared helpers
    // ---------------------------------------------------------------
    private Product findActiveOrThrow(String code) {
        String normalized = code.trim().toUpperCase();
        return productRepository.findByCodeAndDeletedFalse(normalized)
                .orElseThrow(() -> new ProductNotFoundException(normalized));
    }

    private void logHistory(Product product, StockAction action, int previousQuantity, int newQuantity, String note) {
        StockHistory entry = new StockHistory(product.getCode(), product.getName(), action,
                previousQuantity, newQuantity, note);
        stockHistoryRepository.save(entry);
    }
}
