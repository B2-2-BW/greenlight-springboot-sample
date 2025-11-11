package com.winten.greenlight.sample.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping
public class SampleController {

    private Map<String, String> itemMap = Map.of(
            "40A000001", "상품A",
            "40A000002", "상품B",
            "40A000003", "상품C");
    private Map<String, String> descMap = Map.of(
            "40A000001", "상품A 상세 설명입니다.",
            "40A000002", "상품B 상세 설명입니다.",
            "40A000003", "상품C 상세 설명입니다.");

    @GetMapping("/search")
    public String search() {
        return "search";
    }

    @GetMapping("/itemPtc")
    public String itemPtc(@RequestParam String slitmCd, Model model) {
        model.addAttribute("slitmNm", itemMap.get(slitmCd));
        model.addAttribute("slitmDesc", descMap.get(slitmCd));
        model.addAttribute("slitmCd", slitmCd);
        return "itemPtc";
    }

    @PostMapping("/order")
    public String order(HttpServletRequest request, Model model) {
        String slitmCd = request.getParameter("slitmCd");
        model.addAttribute("slitmNm", itemMap.get(slitmCd));
        model.addAttribute("slitmDesc", descMap.get(slitmCd));
        model.addAttribute("slitmCd", slitmCd);
        return "order";
    }

    @PostMapping("/orderComplete")
    public String orderComplete() {
        return "orderComplete";
    }
}