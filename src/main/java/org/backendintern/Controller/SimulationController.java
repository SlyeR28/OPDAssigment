package org.backendintern.Controller;

import lombok.RequiredArgsConstructor;
import org.backendintern.Models.Entities.Doctor;
import org.backendintern.Models.Enums.TokenType;
import org.backendintern.Payloads.Request.CreateSlotRequest;
import org.backendintern.Payloads.Request.CreateTokenRequest;
import org.backendintern.Repository.DoctorRepository;
import org.backendintern.Service.SlotServices;
import org.backendintern.Service.TokenAllocationServices;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/api/simulation")
@RequiredArgsConstructor
public class SimulationController {

    private final DoctorRepository doctorRepository;
    private final SlotServices slotServices;
    private final TokenAllocationServices tokenAllocationServices;
    private final Random random = new Random();

    @PostMapping("/run")
    public String runSimulation() {
        StringBuilder report = new StringBuilder("=== OPD Day Simulation Report ===\n\n");

        // 1. Setup Doctors
        List<Doctor> doctors = doctorRepository.findAll();
        if (doctors.size() < 3) {
            doctorRepository.save(Doctor.builder().name("Dr. Smith").build());
            doctorRepository.save(Doctor.builder().name("Dr. Jones").build());
            doctorRepository.save(Doctor.builder().name("Dr. Taylor").build());
            doctors = doctorRepository.findAll();
        }

        report.append("Doctors Ready: ").append(doctors.size()).append("\n");

        // 2. Create Slots for each doctor
        LocalDateTime todayStart = LocalDateTime.now().plusHours(1);
        for (Doctor doctor : doctors) {
            for (int h = 0; h < 4; h++) {
                CreateSlotRequest slotReq = new CreateSlotRequest();
                slotReq.setDoctorId(doctor.getId());
                slotReq.setStartTime(todayStart.plusHours(h));
                slotReq.setEndTime(todayStart.plusHours(h + 1));
                slotReq.setMaxCapacity(5);
                slotServices.createSlot(slotReq);
            }
        }

        // 3. Simulate Incoming Patients
        TokenType[] types = TokenType.values();
        for (int i = 1; i <= 25; i++) {
            TokenType type = types[random.nextInt(types.length)];
            Doctor doctor = doctors.get(random.nextInt(doctors.size()));

            CreateTokenRequest tokenReq = new CreateTokenRequest();
            tokenReq.setPatientName("Patient " + i);
            tokenReq.setTokenType(type);
            tokenReq.setDoctorId(doctor.getId());

            var response = tokenAllocationServices.allocateToken(tokenReq);
            if (response != null) {
                report.append(String.format("[SUCCESS] %s booked with %s for type %s\n",
                        tokenReq.getPatientName(), doctor.getName(), type));
            } else {
                report.append(String.format("[WAITLIST] %s added to waitlist for %s for type %s\n",
                        tokenReq.getPatientName(), doctor.getName(), type));
            }
        }

        return report.toString();
    }
}
