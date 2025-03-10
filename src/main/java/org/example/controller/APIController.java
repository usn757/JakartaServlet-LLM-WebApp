package org.example.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.model.APIParam;
import org.example.model.ModelType;
import org.example.service.APIService;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;


@WebServlet(name = "APIServlet", value = "/api")
public class APIController extends HttpServlet {

    final APIService apiService = APIService.getInstance();

    final Logger logger = Logger.getLogger(APIController.class.getName());


    @Override
    public void init() throws ServletException {
        logger.info("APIController init");
        super.init();
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        super.doGet(req, resp);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        String prompt = req.getParameter("prompt");
        String modelParam = req.getParameter("model");
        ModelType model = ModelType.valueOf(modelParam);


        APIParam apiParam = new APIParam(prompt, model);

        try {
            out.println(apiService.callAPI(apiParam));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println(e.getMessage());
        }

    }



}
