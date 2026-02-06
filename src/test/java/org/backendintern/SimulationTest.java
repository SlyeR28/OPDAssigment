package org.backendintern;

import jakarta.transaction.Transactional;
import org.backendintern.Models.Entities.Doctor;
import org.backendintern.Models.Enums.TokenType;
import org.backendintern.Payloads.Request.CreateSlotRequest;
import org.backendintern.Payloads.Request.CreateTokenRequest;
import org.backendintern.Repository.DoctorRepository;
import org.backendintern.Service.SlotServices;
import org.backendintern.Service.TokenAllocationServices;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@SpringBootTest
public class SimulationTest {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private org.backendintern.Repository.SlotRepository slotRepository;

    @Autowired
    private SlotServices slotServices;

    @Autowired
    private TokenAllocationServices tokenAllocationServices;

    private final Random random = new Random();

    @Test
    @Transactional
    public void runOpdDaySimulation() {
        System.out.println("\n=== OPD Day Simulation Starting ===\n");

        // 1. Setup Doctors
        doctorRepository.deleteAll();
        doctorRepository.save(Doctor.builder().name("Dr. Smith").build());
        doctorRepository.save(Doctor.builder().name("Dr. Jones").build());
        doctorRepository.save(Doctor.builder().name("Dr. Taylor").build());
        List<Doctor> doctors = doctorRepository.findAll();

        System.out.println("Doctors Ready: " + doctors.size());

        // 2. Create Slots for each doctor
        LocalDateTime todayStart = LocalDateTime.now().minusMinutes(5);
        for (Doctor doctor : doctors) {
            CreateSlotRequest slotReq = new CreateSlotRequest();
            slotReq.setDoctorId(doctor.getId());
            slotReq.setStartTime(todayStart);
            slotReq.setEndTime(todayStart.plusHours(24));
            slotReq.setMaxCapacity(3);
            slotServices.createSlot(slotReq);
            System.out.println("Slot created for " + doctor.getName());
        }

        // Verify slots in DB
        long slotCount = slotRepository.count();
        System.out.println("Total slots in DB: " + slotCount);
        if (slotCount == 0) {
            throw new RuntimeException("Test setup failed: No slots created!");
        }

        // 3. Simulate Incoming Patients (20 patients for slots totaling 9 capacity)
        TokenType[] types = TokenType.values();
        for (int i = 1; i <= 20; i++) {
            TokenType type = types[random.nextInt(types.length)];
            Doctor doctor = doctors.get(random.nextInt(doctors.size()));

            CreateTokenRequest tokenReq = new CreateTokenRequest();
            tokenReq.setPatientName("Patient " + i);
            tokenReq.setTokenType(type);
            tokenReq.setDoctorId(doctor.getId());

            System.out.print(String.format("Patient %d (%s) arrived for %s -> ", i, type, doctor.getName()));
            var response = tokenAllocationServices.allocateToken(tokenReq);
            if (response != null) {
                System.out.println("BOOKED (Slot: " + response.getSlotId() + ")");
            } else {
                System.out.println("WAITLISTED (Priority Waitlist)");
            }
        }

        System.out.println("\n=== OPD Day Simulation Finished ===\n");
    }
}
