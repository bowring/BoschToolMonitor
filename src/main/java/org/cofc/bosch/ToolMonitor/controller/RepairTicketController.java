package org.cofc.bosch.ToolMonitor.controller;

import org.cofc.bosch.ToolMonitor.components.RepairTicket.RepairTicket;
import org.cofc.bosch.ToolMonitor.components.RepairTicket.RepairTicketMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class RepairTicketController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/createOpenRepairTicket")
    public String openRepairTicketForm(Model model) {
        List<String> valueStreams = jdbcTemplate.queryForList("Select Distinct valueStream From WPCCombos;", String.class);
        List<String> prodLines = null;
        List<String> prodTypes = null;
        List<String> repairDets = null;
        List<String> repairCats = null;
        if (!valueStreams.isEmpty()) {
            prodLines = jdbcTemplate.queryForList("Select Distinct productionLine From WPCCombos where valueStream=\""
                    + valueStreams.get(0) + "\";", String.class);
            if (!prodLines.isEmpty()) {
                prodTypes = jdbcTemplate.queryForList("Select Distinct productType From WPCCombos where valueStream=\""
                        + valueStreams.get(0) + "\" and productionLine=\"" + prodLines.get(0) + "\";", String.class);
            }
            if (!prodLines.isEmpty()) {
                repairCats = jdbcTemplate.queryForList("Select Distinct repairCategory From RepairCodes where valueStream=\""
                        + valueStreams.get(0) + "\" and productionLine=\"" + prodLines.get(0) + "\";", String.class);
                if (!repairCats.isEmpty()) {
                    repairDets = jdbcTemplate.queryForList("Select Distinct repairDetail From RepairCodes where valueStream=\""
                            + valueStreams.get(0) + "\" and productionLine=\"" + prodLines.get(0) + "\" and " +
                            "repairCategory=\"" + repairCats.get(0) + "\";", String.class);
                }
            }
        }
        model.addAttribute("valueStreams", valueStreams);
        model.addAttribute("prodLines", prodLines);
        model.addAttribute("prodTypes", prodTypes);
        model.addAttribute("repairCats", repairCats);
        model.addAttribute("repairDets", repairDets);
        model.addAttribute("repairTicket", new RepairTicket());

        return "openRepairTicketForm";
    }

    @PostMapping("/createOpenRepairTicket")
    public String openRepairTicketSubmission(@ModelAttribute RepairTicket repairTicket, Model model) {
        model.addAttribute("repairTicket", repairTicket);
        try {
            repairTicket.enterOpenRepairTicketIntoDatabase(jdbcTemplate);
        } catch (DataAccessException e) {
            model.addAttribute("error", e.getMessage());

            List<String> valueStreams = jdbcTemplate.queryForList("Select Distinct valueStream From WPCCombos;", String.class);
            List<String> prodLines = null;
            List<String> prodTypes = null;
            List<String> repairDets = null;
            List<String> repairCats = null;
            if (!valueStreams.isEmpty()) {
                prodLines = jdbcTemplate.queryForList("Select Distinct productionLine From WPCCombos where valueStream=\""
                        + repairTicket.getValueStream() + "\";", String.class);
                if (!prodLines.isEmpty()) {
                    prodTypes = jdbcTemplate.queryForList("Select Distinct productType From WPCCombos where valueStream=\""
                            + repairTicket.getValueStream() + "\" and productionLine=\"" + repairTicket.getProductionLine() + "\";", String.class);
                }
                if (!prodLines.isEmpty()) {
                    repairCats = jdbcTemplate.queryForList("Select Distinct repairCategory From RepairCodes where valueStream=\""
                            + repairTicket.getValueStream() + "\" and productionLine=\"" + repairTicket.getProductionLine() + "\";", String.class);
                    if (!repairCats.isEmpty()) {
                        repairDets = jdbcTemplate.queryForList("Select Distinct repairDetail From RepairCodes where valueStream=\""
                                + repairTicket.getValueStream() + "\" and productionLine=\"" + repairTicket.getProductionLine() + "\" and " +
                                "repairCategory=\"" + repairTicket.getRepairCategory() + "\";", String.class);
                    }
                }
            }
            model.addAttribute("valueStreams", valueStreams);
            model.addAttribute("prodLines", prodLines);
            model.addAttribute("prodTypes", prodTypes);
            model.addAttribute("repairCats", repairCats);
            model.addAttribute("repairDets", repairDets);

            return "openRepairTicketForm";
        }
        return "openRepairTicketSubmission";
    }

    @GetMapping("/repairTickets")
    public String openedRepairTickets(Model model) {
        List<RepairTicket> repairTickets = jdbcTemplate.query("Select * From RepairTickets", new RepairTicketMapper());
        model.addAttribute("repairTickets", repairTickets);

        return "repairTickets";
    }

    @GetMapping("/repairTicketsForWPC")
    public String openRepairTicketsForWPC(@RequestParam String productionLine, @RequestParam String valueStream,
                                          @RequestParam String productType, @RequestParam int workPieceCarrierNumber, Model model) {
        List<RepairTicket> repairTickets = jdbcTemplate.query("Select * From RepairTickets where valueStream=\"" +
                valueStream + "\" and productionLine=\"" + productionLine + "\" and productType=\"" +
                productType + "\" and workPieceCarrierNumber=" + workPieceCarrierNumber + ";", new RepairTicketMapper());
        model.addAttribute("repairTickets", repairTickets);
        model.addAttribute("isForWPC", true);

        return "repairTickets";
    }

    @GetMapping("/delete_repairTicket")
    public String deleteRepairTicket(@RequestParam String valueStream, @RequestParam String productionLine, @RequestParam String productType,
                                     @RequestParam int workPieceCarrierNumber, @RequestParam String repairCategory, @RequestParam String repairDetail,
                                     @RequestParam String userEntry, @RequestParam String timeStampOpened, Model model) {
        RepairTicket.deleteOpenRepairTicket(jdbcTemplate, valueStream, productionLine, productType, workPieceCarrierNumber, repairCategory, repairDetail, userEntry, timeStampOpened);
        model.addAttribute("repairTickets", jdbcTemplate.query("Select * From RepairTickets", new RepairTicketMapper()));

        return "repairTickets";
    }


    @GetMapping("/closeRepairTicket")
    public String closeRepairTicketForm(@RequestParam String valueStream, @RequestParam String productionLine,
                                        @RequestParam String productType, @RequestParam int workPieceCarrierNumber,
                                        @RequestParam String repairCategory, @RequestParam String repairDetail,
                                        @RequestParam String userEntry, @RequestParam String timeStampOpened, Model model) {
        model.addAttribute("repairTicket", RepairTicket.getRepairTicket(jdbcTemplate, valueStream, productionLine,
                productType, workPieceCarrierNumber, repairCategory, repairDetail, userEntry, timeStampOpened));

        return "closeRepairTicket";
    }

    @PostMapping("/closeRepairTicket")
    public String closeRepairTicket(@ModelAttribute RepairTicket repairTicket, Model model) {
        model.addAttribute("repairTicket", repairTicket);
        try {
            repairTicket.closeSelf(jdbcTemplate);
            return "repairTicketClosedSubmission";
        } catch (DataAccessException e) {
            model.addAttribute("error", e.getMessage());
            return "closeRepairTicket";
        }
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}
