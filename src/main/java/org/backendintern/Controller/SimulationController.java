package org.backendintern.Controller;

import lombok.RequiredArgsConstructor;
import org.backendintern.Models.Entities.Doctor;
import org.backendintern.Models.Enums.TokenType;
import org.backendintern.Payloads.Request.CreateSlotRequest;
import org.backendintern.Payloads.Request.CreateTokenRequest;
import org.backendintern.Payloads.Response.TokenResponse;
import org.backendintern.Repository.DoctorRepository;
import org.backendintern.Service.SlotServices;
import org.backendintern.Service.TokenAllocationServices;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/simulation")
@RequiredArgsConstructor
public class SimulationController {

    private final DoctorRepository doctorRepository;
    private final SlotServices slotServices;
    private final TokenAllocationServices tokenService;
    private final Random random = new Random();

    @PostMapping("/run")
    public Map<String, Object> runSimulation() {

        Map<String, Object> report = new LinkedHashMap<>();

        // ===================== SETUP =====================

        List<Doctor> doctors = setupDoctors();
        setupSlots(doctors);

        // ===================== METRICS =====================

        int totalRequests = 100;
        int allocated = 0;
        int waitlisted = 0;
        int emergencyHandled = 0;

        Map<String, Integer> doctorLoad = new HashMap<>();

        TokenType[] types = TokenType.values();

        // ===================== SIMULATION =====================

        for (int i = 1; i <= totalRequests; i++) {

            Doctor doctor = doctors.get(random.nextInt(doctors.size()));
            TokenType type = types[random.nextInt(types.length)];

            CreateTokenRequest request = new CreateTokenRequest(
                    "Patient-" + i,
                    type,
                    doctor.getId()
            );

            TokenResponse response = tokenService.allocateToken(request);

            if (response.getTokenStatus().name().equals("WAITLISTED")) {
                waitlisted++;
            } else {
                allocated++;
                doctorLoad.merge(doctor.getName(), 1, Integer::sum);
            }

            if (type == TokenType.EMERGENCY) {
                emergencyHandled++;
            }
        }

        // ===================== REPORT =====================

        report.put("total_requests", totalRequests);
        report.put("tokens_allocated", allocated);
        report.put("tokens_waitlisted", waitlisted);
        report.put("emergencies", emergencyHandled);
        report.put("doctor_load", doctorLoad);
        report.put("simulation_time", LocalDateTime.now());

        return report;
    }

    // ===================== HELPERS =====================

    private List<Doctor> setupDoctors() {
        List<Doctor> doctors = doctorRepository.findAll();

        if (doctors.size() < 3) {
            doctors = doctorRepository.saveAll(List.of(
                    Doctor.builder().name("Dr. Alpha").build(),
                    Doctor.builder().name("Dr. Beta").build(),
                    Doctor.builder().name("Dr. Gamma").build()
            ));
        }
        return doctors;
    }

    private void setupSlots(List<Doctor> doctors) {

        LocalDateTime start = LocalDateTime.now().plusHours(1);

        for (Doctor doctor : doctors) {
            for (int i = 0; i < 4; i++) {
                CreateSlotRequest slot = new CreateSlotRequest();
                slot.setDoctorId(doctor.getId());
                slot.setStartTime(start.plusHours(i));
                slot.setEndTime(start.plusHours(i + 1));
                slot.setMaxCapacity(5);
                slotServices.createSlot(slot);
            }
        }
    }
}
