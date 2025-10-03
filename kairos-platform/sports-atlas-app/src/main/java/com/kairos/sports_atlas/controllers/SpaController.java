package com.kairos.sports_atlas.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * A controller to forward all non-API, non-static requests to the
 * root of the single-page application (index.html), allowing React Router
 * to handle the routing.
 */
@Controller
public class SpaController {

    /**
     * Forwards any path that is not a static file and not an API call.
     * The regex '!/api' means 'not starting with /api'.
     * The regex '\\.[^\\.]*$' means 'does not contain a file extension'.
     * @return The forward instruction to the root path.
     */
    @RequestMapping(value = {"/", "/{x:[\\w\\-]+}", "/{x:^(?!api$).*$}/**/{y:[\\w\\-]+}"})
    public String forward() {
        return "forward:/index.html";
    }
}