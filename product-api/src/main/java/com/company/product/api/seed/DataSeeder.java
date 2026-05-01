package com.company.product.api.seed;

import com.company.product.api.entity.*;
import com.company.product.api.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {
    private final AppUserRepository userRepository;
    private final StudentGroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;
    private final DishRepository dishRepository;
    private final MenuItemRepository menuItemRepository;
    private final PasswordEncoder passwordEncoder;
    private final String usersFilePath;

    public DataSeeder(
            AppUserRepository userRepository,
            StudentGroupRepository groupRepository,
            GroupMemberRepository memberRepository,
            DishRepository dishRepository,
            MenuItemRepository menuItemRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.users-file:users.txt}") String usersFilePath
    ) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.dishRepository = dishRepository;
        this.menuItemRepository = menuItemRepository;
        this.passwordEncoder = passwordEncoder;
        this.usersFilePath = usersFilePath;
    }

    @Override
    public void run(String... args) throws Exception {
        AppUser admin = upsertUser("admin1@pgk.local", "Иванов", "Иван", "Иванович", UserRole.ADMIN, "admin123");
        AppUser curator = upsertUser("curator1@pgk.local", "Петрова", "Анна", "Сергеевна", UserRole.CURATOR, "curator123");
        AppUser chef = upsertUser("chef1@pgk.local", "Смирнов", "Олег", "Викторович", UserRole.CHEF, "chef123");
        AppUser student1 = upsertUser("student1@pgk.local", "Кузнецов", "Максим", "Андреевич", UserRole.STUDENT, "student123");
        AppUser student2 = upsertUser("student2@pgk.local", "Соколова", "Мария", "Ильинична", UserRole.STUDENT, "student123");

        StudentGroup group = groupRepository.findAll().stream().findFirst().orElseGet(() -> {
            StudentGroup g = new StudentGroup();
            g.setName("ИС-101");
            g.setCurator(curator);
            return groupRepository.save(g);
        });

        ensureMembership(group, student1);
        ensureMembership(group, student2);

        if (dishRepository.count() == 0) {
            Dish d1 = new Dish();
            d1.setName("Омлет");
            d1.setDescription("Омлет с зеленью");
            d1.setProteinsPer100g(java.math.BigDecimal.valueOf(10));
            d1.setFatsPer100g(java.math.BigDecimal.valueOf(8));
            d1.setCarbsPer100g(java.math.BigDecimal.valueOf(2));
            d1.setCaloriesPer100g(java.math.BigDecimal.valueOf(120));
            d1.setCreatedByChef(chef);
            dishRepository.save(d1);

            Dish d2 = new Dish();
            d2.setName("Борщ");
            d2.setDescription("Классический борщ");
            d2.setProteinsPer100g(java.math.BigDecimal.valueOf(3));
            d2.setFatsPer100g(java.math.BigDecimal.valueOf(4));
            d2.setCarbsPer100g(java.math.BigDecimal.valueOf(7));
            d2.setCaloriesPer100g(java.math.BigDecimal.valueOf(78));
            d2.setCreatedByChef(chef);
            dishRepository.save(d2);

            Dish d3 = new Dish();
            d3.setName("Рыба с рисом");
            d3.setDescription("Запеченная рыба и рис");
            d3.setProteinsPer100g(java.math.BigDecimal.valueOf(14));
            d3.setFatsPer100g(java.math.BigDecimal.valueOf(6));
            d3.setCarbsPer100g(java.math.BigDecimal.valueOf(16));
            d3.setCaloriesPer100g(java.math.BigDecimal.valueOf(170));
            d3.setCreatedByChef(chef);
            dishRepository.save(d3);

            LocalDate today = LocalDate.now();
            addMenu(today, MealSlot.BREAKFAST, d1);
            addMenu(today, MealSlot.LUNCH, d2);
            addMenu(today, MealSlot.DINNER, d3);
        }

        writeUsersFile();
    }

    private AppUser upsertUser(String email, String lastName, String firstName, String middleName, UserRole role, String rawPassword) {
        AppUser user = userRepository.findByEmail(email).orElseGet(AppUser::new);
        user.setEmail(email);
        user.setLastName(lastName);
        user.setFirstName(firstName);
        user.setMiddleName(middleName);
        user.setRole(role);
        user.setActive(true);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        return userRepository.save(user);
    }

    private void ensureMembership(StudentGroup group, AppUser student) {
        memberRepository.findByStudentId(student.getId()).orElseGet(() -> {
            GroupMember member = new GroupMember();
            member.setGroup(group);
            member.setStudent(student);
            return memberRepository.save(member);
        });
    }

    private void addMenu(LocalDate date, MealSlot slot, Dish dish) {
        if (menuItemRepository.findByMenuDateAndMealSlot(date, slot).stream().noneMatch(m -> m.getDish().getId().equals(dish.getId()))) {
            MenuItem item = new MenuItem();
            item.setMenuDate(date);
            item.setMealSlot(slot);
            item.setDish(dish);
            menuItemRepository.save(item);
        }
    }

    private void writeUsersFile() throws IOException {
        List<String> lines = List.of(
                "email=admin1@pgk.local; password=admin123; role=ADMIN; lastName=Иванов; firstName=Иван; middleName=Иванович",
                "email=curator1@pgk.local; password=curator123; role=CURATOR; lastName=Петрова; firstName=Анна; middleName=Сергеевна",
                "email=chef1@pgk.local; password=chef123; role=CHEF; lastName=Смирнов; firstName=Олег; middleName=Викторович",
                "email=student1@pgk.local; password=student123; role=STUDENT; lastName=Кузнецов; firstName=Максим; middleName=Андреевич",
                "email=student2@pgk.local; password=student123; role=STUDENT; lastName=Соколова; firstName=Мария; middleName=Ильинична"
        );
        Path target = Path.of(usersFilePath);
        if (Files.exists(target) && Files.isDirectory(target)) {
            target = target.resolve("users.txt");
        }
        if (target.getParent() != null) {
            Files.createDirectories(target.getParent());
        }
        Files.write(target, lines);
    }
}
