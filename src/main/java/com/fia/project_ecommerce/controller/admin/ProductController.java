package com.fia.project_ecommerce.controller.admin;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import com.fia.project_ecommerce.controller.service.ProductService;
import com.fia.project_ecommerce.controller.service.UploadFileService;
import com.fia.project_ecommerce.model.Product;
import com.fia.project_ecommerce.model.User;

;

@Controller
public class ProductController {
    private final ProductService productService;
    private final UploadFileService uploadFileService;
 
    public ProductController(ProductService productService, UploadFileService uploadFileService) {
        this.productService = productService;
        this.uploadFileService = uploadFileService;
    }

    @GetMapping("/admin/product")
    public String getProduct(
        Model model,
        @RequestParam("page") Optional<String> pageOptional) {
        int page = 1;
        try {
            if (pageOptional.isPresent()) {
                // convert from String to int
                page = Integer.parseInt(pageOptional.get());
            } else {
                // page = 1
            }
        } catch (Exception e) {
            // page = 1
            // TODO: handle exception
        }
         Pageable pageable = PageRequest.of(page - 1, 5);
         Page<Product> prs = this.productService.fetchProducts(pageable);
         List<Product> listProducts = prs.getContent();
         model.addAttribute("products", listProducts);
         model.addAttribute("currentPage", page);
         model.addAttribute("totalPages", prs.getTotalPages());
 
         return "admin/product/show";
     }

    @GetMapping("/admin/product/create") // GET
    public String getCreateProductPage(Model model) {
        model.addAttribute("newProduct", new Product());
        return "admin/product/create";
    }

    @PostMapping("/admin/product/create")
     public String handleCreateProduct(
             @ModelAttribute("newProduct") @Valid Product pr,
             BindingResult newProductBindingResult,
             @RequestParam("uploadFile") MultipartFile file) {
         // validate
         if (newProductBindingResult.hasErrors()) {
             return "admin/product/create";
         }
 
         // upload image
         String image = this.uploadFileService.handleSaveUploadFile(file, "product");
         pr.setImage(image);
 
         this.productService.createProduct(pr);
 
         return "redirect:/admin/product";
     }
    // trang chi tiet san pham
    @GetMapping("/admin/product/{id}")
    public String getUserDetailPage(Model model, @PathVariable long id) {
        Product pr = this.productService.fetchProductById(id).get();// hứng user được trả về từ service
        model.addAttribute("product", pr);// truyền dữ liệu từ controller sang view
        model.addAttribute("id", id);

        return "admin/product/detail";
    }
    // delete product
    @GetMapping("/admin/product/delete/{id}")
    public String getDeleteUser(Model model, @PathVariable long id) {
        model.addAttribute("id", id);
        model.addAttribute("newProduct", new Product());
        return "admin/product/delete";
    }
    @PostMapping("/admin/product/delete")
    public String postDeleteUser(Model model, @ModelAttribute("newUser") User hoidanit) {
        this.productService.deleteProduct(hoidanit.getId());
        return "redirect:/admin/product";
    }
    // update product
    @GetMapping("/admin/product/update/{id}")
     public String getUpdateProductPage(Model model, @PathVariable long id) {
         Optional<Product> currentProduct = this.productService.fetchProductById(id);
         model.addAttribute("newProduct", currentProduct.get());
         return "admin/product/update";
     }
 
     @PostMapping("/admin/product/update")
     public String handleUpdateProduct(@ModelAttribute("newProduct") @Valid Product pr,
             BindingResult newProductBindingResult,
             @RequestParam("chuong") MultipartFile file) {
 
         // validate
         if (newProductBindingResult.hasErrors()) {
             return "admin/product/update";
         }
 
         Product currentProduct = this.productService.fetchProductById(pr.getId()).get();
         if (currentProduct != null) {
             // update new image
             if (!file.isEmpty()) {
                 String img = this.uploadFileService.handleSaveUploadFile(file, "product");
                 currentProduct.setImage(img);
             }
 
             currentProduct.setName(pr.getName());
             currentProduct.setPrice(pr.getPrice());
             currentProduct.setQuantity(pr.getQuantity());
             currentProduct.setDetailDesc(pr.getDetailDesc());
             currentProduct.setShortDesc(pr.getShortDesc());
             currentProduct.setFactory(pr.getFactory());
             currentProduct.setTarget(pr.getTarget());
 
             this.productService.createProduct(currentProduct);
         }
 
         return "redirect:/admin/product";
     }
 }
