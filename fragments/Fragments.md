### Рисунок 2.36 – Реализация контроллера авторизации

### [Скрин кода](./img_1.png)

```java
@PostMapping("/login")
public AuthDtos.AuthResponse login(@Valid @RequestBody AuthDtos.LoginRequest request) {
    return authService.login(request);
}
```

### Рисунок 2.37 – Реализация проверки JWT-токена

### [Скрин кода](./img_2.png)

```java
@Override
protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
    String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        filterChain.doFilter(request, response);
        return;
    }

    String token = authHeader.substring(7);
    Claims claims;
    try {
        claims = jwtService.parse(token);
    } catch (Exception ex) {
        filterChain.doFilter(request, response);
        return;
    }

    String email = claims.getSubject();
    AppUser user = userRepository.findByEmail(email).orElse(null);
    if (user != null && user.isActive() && SecurityContextHolder.getContext().getAuthentication() == null) {
        var auth = new UsernamePasswordAuthenticationToken(
                user,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
    filterChain.doFilter(request, response);
}
```

### Рисунок 2.38 – Запуск интерфейса мобильного приложения

### [Скрин кода](./img_3.png)

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    WindowCompat.setDecorFitsSystemWindows(window, false)
    window.statusBarColor = android.graphics.Color.parseColor("#071018")
    window.navigationBarColor = android.graphics.Color.parseColor("#071018")
    WindowInsetsControllerCompat(window, window.decorView).apply {
        isAppearanceLightStatusBars = false
        isAppearanceLightNavigationBars = false
    }
    setContent {
        AppRoot()
    }
}
```

### Рисунок 2.39 – Реализация функций администратора

### [Скрин кода](./img_4.png)

```java
@PostMapping("/users")
public Object createUser(@Valid @RequestBody RequestDtos.CreateUserRequest request) {
    AppUser user = new AppUser();
    user.setEmail(request.email());
    user.setPasswordHash(passwordEncoder.encode(request.password()));
    user.setFirstName(request.firstName());
    user.setLastName(request.lastName());
    user.setMiddleName(request.middleName());
    user.setRole(request.role());
    user.setActive(true);
    return mapper.toUserDto(userRepository.save(user));
}
```

### Рисунок 2.40 – Реализация функций куратора

### [Скрин кода](./img_5.png)

```java
@PostMapping("/vouchers/issue")
public List<?> issue(@Valid @RequestBody RequestDtos.IssueVoucherRequest request) {
    Long curatorId = currentUserService.requireUser().getId();
    if (!groupMemberRepository.existsByGroupCuratorIdAndStudentId(curatorId, request.studentId())) {
        throw new IllegalArgumentException("Студент не относится к группе куратора");
    }

    AppUser student = userRepository.findById(request.studentId()).orElseThrow();
    AppUser curator = currentUserService.requireUser();

    for (MealSlot slot : request.slots()) {
        voucherRepository.findByStudentIdAndIssueDateAndMealSlot(student.getId(), request.date(), slot).ifPresentOrElse(
                existing -> {},
                () -> {
                    MealVoucher voucher = new MealVoucher();
                    voucher.setStudent(student);
                    voucher.setIssueDate(request.date());
                    voucher.setMealSlot(slot);
                    voucher.setStatus(VoucherStatus.ISSUED);
                    voucher.setIssuedByCurator(curator);
                    voucherRepository.save(voucher);
                }
        );
    }
    return voucherRepository.findByStudentIdOrderByIssueDateDesc(student.getId()).stream().limit(10).map(mapper::toVoucherDto).toList();
}
```

### Рисунок 2.41 – Реализация экрана выдачи талона в мобильном приложении

### [Скрин кода](./img_6.png)

```kotlin
@Composable
fun CuratorIssueVoucherScreen(
    repo: AppRepository,
    studentId: Long,
    onDone: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var student by remember { mutableStateOf<UserDto?>(null) }
    var vouchers by remember { mutableStateOf<List<VoucherDto>>(emptyList()) }
    var date by remember { mutableStateOf(LocalDate.now().toString()) }
    var breakfast by remember { mutableStateOf(true) }
    var lunch by remember { mutableStateOf(true) }
    var dinner by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }

    fun reloadStudentAndVouchers() {
        scope.launch {
            loading = true
            try {
                val students = repo.curatorStudents()
                student = students.firstOrNull { it.id == studentId }
                vouchers = repo.curatorStudentVouchers(studentId)
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(studentId) {
        reloadStudentAndVouchers()
    }

    ScreenContainer("Выдача талона") {
        student?.let {
            Button(
                onClick = {
                    scope.launch {
                        saving = true
                        try {
                            val slots = buildList {
                                if (breakfast) add("BREAKFAST")
                                if (lunch) add("LUNCH")
                                if (dinner) add("DINNER")
                            }
                            repo.issueVouchers(studentId, date, slots)
                            result = "Талоны выданы"
                            vouchers = repo.curatorStudentVouchers(studentId)
                        } catch (e: Exception) {
                            result = "Ошибка: ${e.message}"
                        } finally {
                            saving = false
                        }
                    }
                },
                enabled = !saving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (saving) "Выдаём..." else "Выдать талон")
            }
        }
    }
}
```

### Рисунок 2.42 – Реализация функций студента

### [Скрин кода](./img_7.png)

```kotlin
@Composable
fun StudentVouchersScreen(repo: AppRepository) {
    var vouchers by remember { mutableStateOf<List<VoucherDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("ALL") }
    var sortMode by remember { mutableStateOf("date") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                vouchers = repo.studentVouchers()
            } catch (e: Exception) {
                error = e.message
            } finally {
                loading = false
            }
        }
    }

    val visibleVouchers = remember(vouchers, query, statusFilter, sortMode) {
        vouchers
            .filter { voucher ->
                val matchesQuery = query.isBlank() ||
                    voucher.studentName.contains(query, ignoreCase = true) ||
                    mealSlotLabel(voucher.mealSlot).contains(query, ignoreCase = true) ||
                    voucherStatusLabel(voucher.status).contains(query, ignoreCase = true)
                val matchesFilter = statusFilter == "ALL" || voucher.status == statusFilter
                matchesQuery && matchesFilter
            }
            .sortedWith(compareByDescending<VoucherDto> { it.issueDate }.thenBy { it.id })
    }

    ScreenContainer("Мои талоны") {
        if (loading) CenterLoading()
        if (error != null) ErrorCard(error!!)
        if (visibleVouchers.isEmpty()) {
            EmptyStateCard("Талоны не найдены", "На текущий момент у вас нет доступных талонов")
        } else {
            groupVouchersByDate(visibleVouchers).forEach { group ->
                VoucherDayCard(group = group)
            }
        }
    }
}
```

### Рисунок 2.43 – Реализация функций повара

### [Скрин кода](./img_8.png)

```java
@PostMapping("/scan")
public CommonDtos.ScanResultDto scan(@Valid @RequestBody CommonDtos.QrPayload payload) {
    AppUser student = userRepository.findById(payload.studentId()).orElseThrow();
    List<MealVoucher> vouchers = voucherRepository.findByStudentIdAndIssueDateAndStatus(student.getId(), payload.date(), VoucherStatus.ISSUED);
    List<MenuItem> menu = menuItemRepository.findByMenuDateOrderByMealSlotAsc(payload.date());
    List<MenuItem> allowedMenu = menu.stream().filter(mi ->
            vouchers.stream().anyMatch(v -> v.getMealSlot() == mi.getMealSlot())
    ).toList();
    return new CommonDtos.ScanResultDto(
            mapper.toUserDto(student),
            vouchers.stream().map(mapper::toVoucherDto).toList(),
            allowedMenu.stream().map(mapper::toMenuItemDto).toList()
    );
}
```

### Рисунок 2.44 – Реализация формы создания блюда

### [Скрин кода](./img_9.png)

```kotlin
@Composable
fun ChefDishCreateScreen(
    repo: AppRepository,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var proteins by remember { mutableStateOf("") }
    var fats by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var selectedPhoto by remember { mutableStateOf<Uri?>(null) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    ScreenContainer("Создать блюдо") {
        Button(
            onClick = {
                if (name.isBlank()) {
                    error = "Укажите название блюда"
                    return@Button
                }
                scope.launch {
                    saving = true
                    try {
                        repo.chefCreateDish(
                            context = context,
                            name = name.trim(),
                            description = description.trim().takeIf { it.isNotBlank() },
                            proteinsPer100g = proteins.trim().takeIf { it.isNotBlank() },
                            fatsPer100g = fats.trim().takeIf { it.isNotBlank() },
                            carbsPer100g = carbs.trim().takeIf { it.isNotBlank() },
                            caloriesPer100g = calories.trim().takeIf { it.isNotBlank() },
                            photoUri = selectedPhoto
                        )
                        onDone()
                    } finally {
                        saving = false
                    }
                }
            },
            enabled = !saving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Создать блюдо")
        }
    }
}
```

### Рисунок 2.45 – Реализация сканирования QR-кода

### [Скрин кода](./img_10.png)

```kotlin
private fun processFrame(
    imageProxy: ImageProxy,
    barcodeScanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    onQrScanned: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage == null) {
        imageProxy.close()
        return
    }

    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
    barcodeScanner.process(image)
        .addOnSuccessListener { barcodes ->
            val raw = barcodes.firstOrNull()?.rawValue
            if (!raw.isNullOrBlank()) {
                onQrScanned(raw)
            }
        }
        .addOnCompleteListener {
            imageProxy.close()
        }
}
```

### Рисунок 2.46 – Реализация работы с файлами профиля

### [Скрин кода](./img_11.png)

```java
@PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public Object uploadAvatar(@RequestPart("file") MultipartFile file) {
    AppUser user = currentUserService.requireUser();
    user.setAvatarPath(storageService.store(file, "avatars"));
    userRepository.save(user);
    return mapper.toUserDto(user);
}
```

### Рисунок 2.47 – РеализацияAPI-клиента мобильного приложения

### [Скрин кода](./img_12.png)

```kotlin
interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @GET("api/auth/me")
    suspend fun me(): UserDto

    @GET("api/student/vouchers")
    suspend fun studentVouchers(): List<VoucherDto>

    @POST("api/curator/vouchers/issue")
    suspend fun issueVoucher(@Body body: IssueVoucherRequest): List<VoucherDto>

    @POST("api/chef/scan")
    suspend fun chefScan(@Body body: QrPayload): ScanResultDto

    @Multipart
    @POST("api/chef/dishes")
    suspend fun chefCreateDish(
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody?,
        @Part file: MultipartBody.Part?
    ): DishDto

    @GET("api/admin/users")
    suspend fun adminUsers(): List<UserDto>

    @Multipart
    @POST("api/profile/avatar")
    suspend fun uploadAvatar(@Part file: MultipartBody.Part): UserDto
}
```

### Листинг кода программного продукта страниц на 3-4.

```java
@PostMapping("/users")
public Object createUser(@Valid @RequestBody RequestDtos.CreateUserRequest request) {
    AppUser user = new AppUser();
    user.setEmail(request.email());
    user.setPasswordHash(passwordEncoder.encode(request.password()));
    user.setFirstName(request.firstName());
    user.setLastName(request.lastName());
    user.setMiddleName(request.middleName());
    user.setRole(request.role());
    user.setActive(true);
    return mapper.toUserDto(userRepository.save(user));
}

@PostMapping("/groups")
public Object createGroup(@Valid @RequestBody RequestDtos.CreateGroupRequest request) {
    StudentGroup group = new StudentGroup();
    group.setName(request.name());
    if (request.curatorId() != null) {
        AppUser curator = userRepository.findById(request.curatorId()).orElseThrow();
        if (curator.getRole() != UserRole.CURATOR) {
            throw new IllegalArgumentException("Пользователь не является куратором");
        }
        group.setCurator(curator);
    }
    return mapper.toGroupDto(groupRepository.save(group));
}

@PostMapping("/vouchers/issue")
public List<?> issue(@Valid @RequestBody RequestDtos.IssueVoucherRequest request) {
    Long curatorId = currentUserService.requireUser().getId();
    if (!groupMemberRepository.existsByGroupCuratorIdAndStudentId(curatorId, request.studentId())) {
        throw new IllegalArgumentException("Студент не относится к группе куратора");
    }

    AppUser student = userRepository.findById(request.studentId()).orElseThrow();
    AppUser curator = currentUserService.requireUser();

    for (MealSlot slot : request.slots()) {
        voucherRepository.findByStudentIdAndIssueDateAndMealSlot(student.getId(), request.date(), slot).ifPresentOrElse(
                existing -> {},
                () -> {
                    MealVoucher voucher = new MealVoucher();
                    voucher.setStudent(student);
                    voucher.setIssueDate(request.date());
                    voucher.setMealSlot(slot);
                    voucher.setStatus(VoucherStatus.ISSUED);
                    voucher.setIssuedByCurator(curator);
                    voucherRepository.save(voucher);
                }
        );
    }
    return voucherRepository.findByStudentIdOrderByIssueDateDesc(student.getId()).stream().limit(10).map(mapper::toVoucherDto).toList();
}

@PostMapping(value = "/dishes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public Object createDishMultipart(
        @RequestParam @jakarta.validation.constraints.NotBlank String name,
        @RequestParam(required = false) String description,
        @RequestParam(required = false) BigDecimal proteinsPer100g,
        @RequestParam(required = false) BigDecimal fatsPer100g,
        @RequestParam(required = false) BigDecimal carbsPer100g,
        @RequestParam(required = false) BigDecimal caloriesPer100g,
        @RequestPart(required = false) MultipartFile file
) {
    Dish dish = new Dish();
    return mapper.toDishDto(saveDish(
            dish,
            name,
            description,
            proteinsPer100g,
            fatsPer100g,
            carbsPer100g,
            caloriesPer100g,
            file
    ));
}

@PostMapping("/scan")
public CommonDtos.ScanResultDto scan(@Valid @RequestBody CommonDtos.QrPayload payload) {
    AppUser student = userRepository.findById(payload.studentId()).orElseThrow();
    List<MealVoucher> vouchers = voucherRepository.findByStudentIdAndIssueDateAndStatus(student.getId(), payload.date(), VoucherStatus.ISSUED);
    List<MenuItem> menu = menuItemRepository.findByMenuDateOrderByMealSlotAsc(payload.date());
    List<MenuItem> allowedMenu = menu.stream().filter(mi ->
            vouchers.stream().anyMatch(v -> v.getMealSlot() == mi.getMealSlot())
    ).toList();
    return new CommonDtos.ScanResultDto(
            mapper.toUserDto(student),
            vouchers.stream().map(mapper::toVoucherDto).toList(),
            allowedMenu.stream().map(mapper::toMenuItemDto).toList()
    );
}

@PostMapping("/redeem")
public Object redeem(@Valid @RequestBody RequestDtos.RedeemRequest request) {
    MealVoucher voucher = voucherRepository.findById(request.voucherId()).orElseThrow();
    if (voucher.getStatus() != VoucherStatus.ISSUED) {
        throw new IllegalStateException("Талон уже погашен");
    }
    voucher.setStatus(VoucherStatus.REDEEMED);
    voucher.setRedeemedByChef(currentUserService.requireUser());
    voucher.setRedeemedAt(LocalDateTime.now());
    return mapper.toVoucherDto(voucherRepository.save(voucher));
}

@PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public Object uploadAvatar(@RequestPart("file") MultipartFile file) {
    AppUser user = currentUserService.requireUser();
    user.setAvatarPath(storageService.store(file, "avatars"));
    userRepository.save(user);
    return mapper.toUserDto(user);
}

@DeleteMapping("/avatar")
public Object deleteAvatar() {
    AppUser user = currentUserService.requireUser();
    user.setAvatarPath(null);
    userRepository.save(user);
    return mapper.toUserDto(user);
}
```
