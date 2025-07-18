package com.fia.project_ecommerce.controller.client;

import java.util.ArrayList;
import java.util.List;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fia.project_ecommerce.controller.service.ProductService;
import com.fia.project_ecommerce.model.Product;
import com.fia.project_ecommerce.model.Product_;
import com.fia.project_ecommerce.model.User;
import com.fia.project_ecommerce.model.cart;
import com.fia.project_ecommerce.model.cartDetail;
import com.fia.project_ecommerce.model.dto.ProductCriteriaDTO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;





@Controller
public class ItemController {
    private final ProductService productService;
    
    public ItemController(ProductService productService) {
        this.productService = productService;
    }

    //  xem chi tiết sản phẩm
    @GetMapping("/product/{id}")
    public String getMethodName(Model model, @PathVariable long id) {
        Product pr = this.productService.fetchProductById(id).get();// hứng user được trả về từ service
        model.addAttribute("product", pr);// truyền dữ liệu từ controller sang view
        model.addAttribute("id", id);
        return "client/product/detail";
    }
    // thêm sản phẩm vào giỏ hàng
    @PostMapping("/add-product-to-cart/{id}")
     public String addProductToCart(@PathVariable long id, HttpServletRequest request) {
         HttpSession session = request.getSession(false);
 
         long productId = id;
         String email = (String) session.getAttribute("email");
 
         this.productService.handleAddProductToCart(email, productId, session, 1);
 
         return "redirect:/";
     }
     // get cart
     @GetMapping("/cart")
     public String getCartPage(Model model, HttpServletRequest request) {
        User currentUser = new User();// null
        HttpSession session = request.getSession(false);// lấy id session đã có gán cho user mới tạo
        long id = (long) session.getAttribute("id");
        currentUser.setId(id);

        cart cart = this.productService.fetchByUser(currentUser);
        // lấy ra giỏ hàng của userCurrent 

        List<cartDetail> cartDetails = cart == null ? new ArrayList<cartDetail>() : cart.getCartDetail();

        double totalPrice = 0;
        for (cartDetail cd : cartDetails) {
            totalPrice += cd.getPrice() * cd.getQuantity();
        }

        model.addAttribute("cartDetails", cartDetails);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("cart", cart);
        return "client/cart/show";
    }
    @PostMapping("/delete-cart-product/{id}")
     public String deleteCartDetail(@PathVariable long id, HttpServletRequest request) {
         HttpSession session = request.getSession(false);
         long cartDetailId = id;
         this.productService.handleRemoveCartDetail(cartDetailId, session);
         return "redirect:/cart";
     }
      @GetMapping("/checkout")
     public String getCheckOutPage(Model model, HttpServletRequest request) {
         User currentUser = new User();// null
         HttpSession session = request.getSession(false);
         long id = (long) session.getAttribute("id");
         currentUser.setId(id);
 
         cart cart = this.productService.fetchByUser(currentUser);
 
         List<cartDetail> cartDetails = cart == null ? new ArrayList<cartDetail>() : cart.getCartDetail();
 
         double totalPrice = 0;
         for (cartDetail cd : cartDetails) {
             totalPrice += cd.getPrice() * cd.getQuantity();
         }
 
         model.addAttribute("cartDetails", cartDetails);
         model.addAttribute("totalPrice", totalPrice);
 
         return "client/cart/checkout";
     }
 
     @PostMapping("/confirm-checkout")
     public String getCheckOutPage(@ModelAttribute("cart") cart cart) {
         List<cartDetail> cartDetails = cart == null ? new ArrayList<cartDetail>() : cart.getCartDetail();
         this.productService.handleUpdateCartBeforeCheckout(cartDetails);
         return "redirect:/checkout";
     }
 
     @PostMapping("/place-order")
     public String handlePlaceOrder(
             HttpServletRequest request,
             @RequestParam("receiverName") String receiverName,
             @RequestParam("receiverAddress") String receiverAddress,
             @RequestParam("receiverPhone") String receiverPhone) {
        User userCurrent = new User();
         HttpSession session = request.getSession(false);
        long id = (long) session.getAttribute("id");
        userCurrent.setId(id);
        this.productService.handlerPlaceOrder(userCurrent, session, receiverName, receiverAddress, receiverPhone);

         return "redirect:/thanks";
     }

     // redirect to thanks sau khi thanh toán thành công
     @GetMapping("/thanks")
     public String getThanksPage() {
         return "client/cart/thanks";
     }
     @PostMapping("/add-product-from-view-detail")
     public String handleAddProductFromViewDetail(
             @RequestParam("id") long id,
             @RequestParam("quantity") long quantity,
             HttpServletRequest request) {
         HttpSession session = request.getSession(false);
 
         String email = (String) session.getAttribute("email");
         this.productService.handleAddProductToCart(email, id, session, quantity);
         return "redirect:/product/" + id;
     }
      @GetMapping("/products")
    public String getProductPage(Model model,
            ProductCriteriaDTO productCriteriaDTO,
            HttpServletRequest request) {
        int page = 1;
        try {
            if (productCriteriaDTO.getPage().isPresent()) {
                // convert from String to int
                page = Integer.parseInt(productCriteriaDTO.getPage().get());
            } else {
                // page = 1
            }
        } catch (Exception e) {
            // page = 1
            // TODO: handle exception
        }

        // check sort price
        Pageable pageable = PageRequest.of(page - 1, 6);

        if (productCriteriaDTO.getSort() != null && productCriteriaDTO.getSort().isPresent()) {
            String sort = productCriteriaDTO.getSort().get();
            if (sort.equals("gia-tang-dan")) {
                pageable = PageRequest.of(page - 1, 10, Sort.by(Product_.PRICE).ascending());
            } else if (sort.equals("gia-giam-dan")) {
                pageable = PageRequest.of(page - 1, 10, Sort.by(Product_.PRICE).descending());
            }
        }

        Page<Product> prs = this.productService.fetchProductsWithSpec(pageable, productCriteriaDTO);

        List<Product> products = prs.getContent().size() > 0 ? prs.getContent()
                : new ArrayList<Product>();

        String qs = request.getQueryString();
        if (qs != null && !qs.isBlank()) {
            // remove page
            qs = qs.replace("page=" + page, "");
        }

        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", prs.getTotalPages());
        model.addAttribute("queryString", qs);
        return "client/product/show";
    }
}
