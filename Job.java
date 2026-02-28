
import java.awt.Desktop;
import java.io.*;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.scene.control.Alert.AlertType;
 

public class Job extends Application {

    // Properties for job seeker application form
    SimpleStringProperty phone = new SimpleStringProperty();
    SimpleStringProperty city = new SimpleStringProperty();
    SimpleStringProperty country = new SimpleStringProperty();
    SimpleStringProperty linkedin = new SimpleStringProperty();
    SimpleStringProperty jobTitle = new SimpleStringProperty();
    SimpleStringProperty experience = new SimpleStringProperty();
    SimpleStringProperty domain = new SimpleStringProperty();
    SimpleStringProperty skills = new SimpleStringProperty();
    SimpleStringProperty certifications = new SimpleStringProperty();
    SimpleStringProperty languages = new SimpleStringProperty();
    SimpleStringProperty degree = new SimpleStringProperty();
    SimpleStringProperty university = new SimpleStringProperty();
    SimpleStringProperty specialization = new SimpleStringProperty();
    SimpleStringProperty graduationYear = new SimpleStringProperty();
    SimpleStringProperty gpa = new SimpleStringProperty();
    SimpleStringProperty website = new SimpleStringProperty();
    SimpleStringProperty desiredTitle = new SimpleStringProperty();
    SimpleStringProperty preferredLocation = new SimpleStringProperty();
    SimpleStringProperty employmentType = new SimpleStringProperty();
    SimpleStringProperty expectedSalary = new SimpleStringProperty();
    SimpleStringProperty availability = new SimpleStringProperty();
    SimpleStringProperty relocation = new SimpleStringProperty();

    File profilePictureFile = null;
    File resumeFile = null;
    File portfolioFile = null;

    // Store current job seeker and company data
    private User currentUser;
    private String currentCompanyName;
    private JobPosting currentPosting;
    private Image companyLogoImage;
    private List<JobPosting> jobPostings = new ArrayList<>();
    private List<User> users = new ArrayList<>();
    private List<Company> companies = new ArrayList<>();
    private List<Message> messages = new ArrayList<>();
    private List<JobPosting> deletedJobs = new ArrayList<>();
    private List<ApplicationForm> applicationForms = new ArrayList<>();
    private File companyLogoFile;

    // Admin companies for Tureth Employer Checker
    private List<AdminCompany> adminCompanies = new ArrayList<>(Arrays.asList(
            new AdminCompany("Ethio Telecom", "123456789"),
            new AdminCompany("Safaricom Ethiopia", "987654321"),
            new AdminCompany("ArifPay", "112233445"),
            new AdminCompany("Yegna Developers", "556677889"),
            new AdminCompany("EfoyTech", "998877665"),
            new AdminCompany("Kifiya Financial Technology", "123123123"),
            new AdminCompany("Ebirr", "456456456"),
            new AdminCompany("Orbit Software Solutions", "789789789")
    ));

    // Message class for storing messages
    public static class Message implements Serializable {
        private static final long serialVersionUID = 1L;
        String sender, recipient, companyName, jobTitle, content;
        String logoFilePath;

        public Message(String sender, String recipient, String companyName, String jobTitle, String content, String logoFilePath) {
            this.sender = sender;
            this.recipient = recipient;
            this.companyName = companyName;
            this.jobTitle = jobTitle;
            this.content = content;
            this.logoFilePath = logoFilePath;
        }
    }

    // User class for storing signup data
    public static class User implements Serializable {
        private static final long serialVersionUID = 1L;
        String name, email, password, userType, status;
        String companyName;
        String profileImagePath;
        List<String> savedJobs;

        public User(String name, String email, String password, String userType, String companyName, String profileImagePath) {
            this.name = name;
            this.email = email;
            this.password = password;
            this.userType = userType;
            this.companyName = companyName;
            this.profileImagePath = profileImagePath;
            this.savedJobs = new ArrayList<>();
            this.status = "Pending";
        }
    }

    // Company class for storing employer company details
    public static class Company implements Serializable {
        private static final long serialVersionUID = 1L;
        String name, description, logoFilePath;

        public Company(String name, String description, String logoFilePath) {
            this.name = name;
            this.description = description;
            this.logoFilePath = logoFilePath;
        }
    }

    // AdminCompany class for Tureth Employer Checker
    public static class AdminCompany implements Serializable {
        private static final long serialVersionUID = 1L;
        String name;
        String id;

        public AdminCompany(String name, String id) {
            this.name = name;
            this.id = id;
        }
    }

    // JobPosting class
    public static class JobPosting implements Serializable {
        private static final long serialVersionUID = 1L;
        String title, description, jobType, qualifications, skills, salaryRange, location, method;
        String companyName;
        transient Image companyLogo;
        String logoFilePath;
        int vacancies, experience;
        LocalDate deadline;
        LocalDateTime postedDate;
        List<String> applicants = new ArrayList<>();

        public JobPosting(String title, String description, String jobType, int vacancies,
                         String qualifications, String skills, int experience,
                         String salaryRange, LocalDate deadline, String location,
                         String method, String companyName, Image companyLogo, String logoFilePath) {
            this.title = title;
            this.description = description;
            this.jobType = jobType;
            this.vacancies = vacancies;
            this.qualifications = qualifications;
            this.skills = skills;
            this.experience = experience;
            this.salaryRange = salaryRange;
            this.deadline = deadline;
            this.location = location;
            this.method = method;
            this.companyName = companyName;
            this.companyLogo = companyLogo;
            this.logoFilePath = logoFilePath;
            this.postedDate = LocalDateTime.now();
        }

        private void writeObject(ObjectOutputStream out) throws IOException {
            out.defaultWriteObject();
            if (companyLogo != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(SwingFXUtils.fromFXImage(companyLogo, null), "png", baos);
                out.writeObject(baos.toByteArray());
            } else {
                out.writeObject(null);
            }
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            byte[] imageBytes = (byte[]) in.readObject();
            if (imageBytes != null) {
                companyLogo = new Image(new ByteArrayInputStream(imageBytes));
            }
        }
    }

    // ApplicationForm class for custom application forms
    public static class ApplicationForm implements Serializable {
        private static final long serialVersionUID = 1L;
        String companyName;
        String jobTitle;
        List<String> fields;

        public ApplicationForm(String companyName, String jobTitle, List<String> fields) {
            this.companyName = companyName;
            this.jobTitle = jobTitle;
            this.fields = fields;
        }
    }

    // Validation patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+251[97]\\d{8}$");
    private static final Pattern URL_PATTERN = Pattern.compile("^(https?://)?([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?$");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^\\d+$");
    private static final Pattern GPA_PATTERN = Pattern.compile("^\\d*\\.?\\d{1,2}$");

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        loadDataFromFile();
        cleanUpExpiredJobs();
        showMainWindow(primaryStage);
    }

    private void cleanUpExpiredJobs() {
        LocalDate today = LocalDate.now();
        Iterator<JobPosting> iterator = jobPostings.iterator();
        while (iterator.hasNext()) {
            JobPosting job = iterator.next();
            if (job.deadline.isBefore(today)) {
                deletedJobs.add(job);
                iterator.remove();
            }
        }
        saveDataToFile();
    }

    private void showMainWindow(Stage stage) {
        Image backgroundImage;
        InputStream imageStream = getClass().getResourceAsStream("default_logo.jpg");
        if (imageStream != null) {
            backgroundImage = new Image(imageStream);
        } else {
            System.out.println("Background image not found, using default background.");
            InputStream defaultStream = getClass().getResourceAsStream("/default_background.png");
            if (defaultStream != null) {
                backgroundImage = new Image(defaultStream);
            } else {
                System.out.println("Default background image not found, skipping background.");
                backgroundImage = null;
            }
        }

        StackPane root = new StackPane();
        if (backgroundImage != null) {
            ImageView backgroundView = new ImageView(backgroundImage);
            backgroundView.setFitWidth(800);
            backgroundView.setFitHeight(600);
            backgroundView.setPreserveRatio(false);
            root.getChildren().add(backgroundView);
        }

        Label titleLabel = new Label("WELCOME TO JOB PORTAL");
        titleLabel.setStyle("-fx-font-size: 60px; -fx-font-weight: bold; -fx-text-fill: dodgerblue; -fx-font-family;roman");

        Button signupButton = new Button("Sign Up");
        signupButton.setStyle("-fx-font-size: 20px; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10 20; -fx-background-radius: 10;");
        signupButton.setTooltip(new Tooltip("If you're new here, please sign up first."));

        Button loginButton = new Button("Login");
        loginButton.setStyle("-fx-font-size: 20px; -fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 10 20; -fx-background-radius: 10;");
        loginButton.setTooltip(new Tooltip("Already have an account? Click here to log in."));

        Button adminButton = new Button("Admin");
        adminButton.setStyle("-fx-font-size: 20px; -fx-background-color: #FF4444; -fx-text-fill: white; -fx-padding: 10 20; -fx-background-radius: 10;");
        adminButton.setOnAction(e -> showAdminLoginWindow());

        signupButton.setOnAction(e -> showSignUpWindow(new Stage()));
        loginButton.setOnAction(e -> showLoginWindow(new Stage()));

        HBox buttonBox = new HBox(40, signupButton, loginButton);
        buttonBox.setStyle("-fx-alignment: center;");

        VBox contentBox = new VBox(40, titleLabel, buttonBox);
        contentBox.setStyle("-fx-alignment: center; -fx-background-color: transparent;");
        contentBox.setPadding(new Insets(30));

        root.getChildren().addAll(contentBox, adminButton);
        StackPane.setAlignment(adminButton, Pos.TOP_RIGHT);
        StackPane.setMargin(adminButton, new Insets(10));

        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.setTitle("Job Portal");
        stage.show();
    }

    private void showAdminLoginWindow() {
        Stage adminLoginStage = new Stage();
        adminLoginStage.initModality(Modality.APPLICATION_MODAL);

        Label passwordLabel = new Label("Admin Password:");
        PasswordField passwordField = new PasswordField();

        Button loginButton = new Button("Login");
        loginButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        loginButton.setOnAction(e -> {
            String password = passwordField.getText();
            if ("job1234".equals(password)) {
                adminLoginStage.close();
                showAdminDashboard();
            } else {
                showAlert("Incorrect password. Please try again.");
            }
        });

        VBox layout = new VBox(10, passwordLabel, passwordField, loginButton);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #f5f5f5;");

        Scene scene = new Scene(layout, 300, 250);
        adminLoginStage.setScene(scene);
        adminLoginStage.setTitle("Admin Login");
        adminLoginStage.show();
    }

    private void showAdminDashboard() {
        Stage dashboardStage = new Stage();

        Label titleLabel = new Label("Admin Dashboard");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // User Management Section
        ComboBox<User> userCombo = new ComboBox<>();
        userCombo.getItems().addAll(users);
        userCombo.setPromptText("Select User");
        userCombo.setCellFactory(lv -> new ListCell<User>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.name + " (" + item.userType + ", " + item.status + ")");
            }
        });
        userCombo.setButtonCell(new ListCell<User>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "Select User" : item.name + " (" + item.userType + ", " + item.status + ")");
            }
        });

        Button approveButton = new Button("Approve User");
        Button rejectButton = new Button("Reject User");
        Button blockButton = new Button("Block User");
        Button deleteButton = new Button("Delete User");
        Button unblockButton = new Button("Unblock User");

        approveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        rejectButton.setStyle("-fx-background-color: #FF4444; -fx-text-fill: white;");
        blockButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
        deleteButton.setStyle("-fx-background-color: #FF4444; -fx-text-fill: white;");
        unblockButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");

        approveButton.setDisable(true);
        rejectButton.setDisable(true);
        blockButton.setDisable(true);
        deleteButton.setDisable(true);
        unblockButton.setDisable(true);

        userCombo.setOnAction(e -> {
            User selectedUser = userCombo.getValue();
            boolean hasSelection = selectedUser != null;
            approveButton.setDisable(!hasSelection || !selectedUser.status.equals("Pending"));
            rejectButton.setDisable(!hasSelection || !selectedUser.status.equals("Pending"));
            blockButton.setDisable(!hasSelection || !selectedUser.status.equals("Approved"));
            deleteButton.setDisable(!hasSelection);
            unblockButton.setDisable(!hasSelection || !selectedUser.status.equals("Blocked"));
        });

        approveButton.setOnAction(e -> {
            User selectedUser = userCombo.getValue();
            if (selectedUser != null && selectedUser.status.equals("Pending")) {
                selectedUser.status = "Approved";
                saveDataToFile();
                showAlert("User " + selectedUser.name + " approved.");
                userCombo.getItems().setAll(users);
            }
        });

        rejectButton.setOnAction(e -> {
            User selectedUser = userCombo.getValue();
            if (selectedUser != null && selectedUser.status.equals("Pending")) {
                selectedUser.status = "Rejected";
                saveDataToFile();
                showAlert("User " + selectedUser.name + " rejected.");
                userCombo.getItems().setAll(users);
            }
        });

        blockButton.setOnAction(e -> {
            User selectedUser = userCombo.getValue();
            if (selectedUser != null && selectedUser.status.equals("Approved")) {
                boolean isInactive = false;
                if (selectedUser.userType.equals("Job Seeker")) {
                    boolean hasActivity = selectedUser.savedJobs.size() > 0 ||
                            jobPostings.stream().anyMatch(p -> p.applicants.contains(selectedUser.name));
                    isInactive = !hasActivity;
                } else if (selectedUser.userType.equals("Employer")) {
                    isInactive = jobPostings.stream().noneMatch(p -> p.companyName.equals(selectedUser.companyName));
                }
                if (isInactive) {
                    selectedUser.status = "Blocked";
                    saveDataToFile();
                    showAlert("User " + selectedUser.name + " blocked due to inactivity.");
                    userCombo.getItems().setAll(users);
                } else {
                    showAlert("Cannot block active user. Please review their activity.");
                }
            }
        });

        deleteButton.setOnAction(e -> {
            User selectedUser = userCombo.getValue();
            if (selectedUser != null) {
                Alert confirm = new Alert(AlertType.CONFIRMATION, "Are you sure you want to delete " + selectedUser.name + "?");
                if (confirm.showAndWait().get().getButtonData().isDefaultButton()) {
                    users.remove(selectedUser);
                    saveDataToFile();
                    showAlert("User " + selectedUser.name + " deleted.");
                    userCombo.getItems().setAll(users);
                    userCombo.setValue(null);
                }
            }
        });

        unblockButton.setOnAction(e -> {
            User selectedUser = userCombo.getValue();
            if (selectedUser != null && selectedUser.status.equals("Blocked")) {
                selectedUser.status = "Approved";
                saveDataToFile();
                showAlert("User " + selectedUser.name + " unblocked.");
                userCombo.getItems().setAll(users);
            }
        });

        HBox userButtonBox = new HBox(10, approveButton, rejectButton, blockButton, unblockButton, deleteButton);
        userButtonBox.setAlignment(Pos.CENTER);

        // Company Management Section
        Label companyTitleLabel = new Label("Manage Tureth Employer Companies");
        companyTitleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        ComboBox<AdminCompany> companyCombo = new ComboBox<>();
        companyCombo.getItems().addAll(adminCompanies);
        companyCombo.setPromptText("Select Company");
        companyCombo.setCellFactory(lv -> new ListCell<AdminCompany>() {
            @Override
            protected void updateItem(AdminCompany item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.name + " (ID: " + item.id + ")");
            }
        });
        companyCombo.setButtonCell(new ListCell<AdminCompany>() {
            @Override
            protected void updateItem(AdminCompany item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "Select Company" : item.name + " (ID: " + item.id + ")");
            }
        });

        Label newCompanyLabel = new Label("New Company Name:");
        TextField newCompanyField = new TextField();
        Label newIdLabel = new Label("New Company ID (9 digits):");
        TextField newIdField = new TextField();

        Button addCompanyButton = new Button("Add Company");
        Button deleteCompanyButton = new Button("Delete Company");

        addCompanyButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        deleteCompanyButton.setStyle("-fx-background-color: #FF4444; -fx-text-fill: white;");
        deleteCompanyButton.setDisable(true);

        companyCombo.setOnAction(e -> {
            AdminCompany selectedCompany = companyCombo.getValue();
            deleteCompanyButton.setDisable(selectedCompany == null);
        });

        addCompanyButton.setOnAction(e -> {
            String companyName = newCompanyField.getText().trim();
            String companyId = newIdField.getText().trim();

            if (companyName.isEmpty()) {
                showAlert("Company name cannot be empty.");
                return;
            }
            if (!companyId.matches("\\d{9}")) {
                showAlert("Company ID must be a 9-digit number.");
                return;
            }
            if (adminCompanies.stream().anyMatch(c -> c.name.equals(companyName) || c.id.equals(companyId))) {
                showAlert("Company name or ID already exists.");
                return;
            }

            adminCompanies.add(new AdminCompany(companyName, companyId));
            saveDataToFile();
            showAlert("Company " + companyName + " added.");
            companyCombo.getItems().setAll(adminCompanies);
            newCompanyField.clear();
            newIdField.clear();
        });

        deleteCompanyButton.setOnAction(e -> {
            AdminCompany selectedCompany = companyCombo.getValue();
            if (selectedCompany != null) {
                Alert confirm = new Alert(AlertType.CONFIRMATION, "Are you sure you want to delete " + selectedCompany.name + "?");
                if (confirm.showAndWait().get().getButtonData().isDefaultButton()) {
                    adminCompanies.remove(selectedCompany);
                    saveDataToFile();
                    showAlert("Company " + selectedCompany.name + " deleted.");
                    companyCombo.getItems().setAll(adminCompanies);
                    companyCombo.setValue(null);
                }
            }
        });

        VBox companyAddBox = new VBox(10, newCompanyLabel, newCompanyField, newIdLabel, newIdField, addCompanyButton);
        HBox companyButtonBox = new HBox(10, companyCombo, deleteCompanyButton);
        companyButtonBox.setAlignment(Pos.CENTER);

        Button logoutButton = new Button("Logout");
        logoutButton.setStyle("-fx-background-color: #FF4444; -fx-text-fill: white;");
        logoutButton.setOnAction(e -> {
            dashboardStage.close();
            showMainWindow(new Stage());
        });

        VBox layout = new VBox(20, titleLabel, userCombo, userButtonBox,
                companyTitleLabel, companyAddBox, companyButtonBox);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #f5f5f5;");

        StackPane root = new StackPane(layout, logoutButton);
        StackPane.setAlignment(logoutButton, Pos.TOP_RIGHT);
        StackPane.setMargin(logoutButton, new Insets(10));

        Scene scene = new Scene(root, 600, 500);
        dashboardStage.setScene(scene);
        dashboardStage.setTitle("Admin Dashboard");
        dashboardStage.show();
    }

    private void showSignUpWindow(Stage stage) {
        Label roleLabel = new Label("Select Role:");
        RadioButton jobSeekerRadio = new RadioButton("Job Seeker");
        RadioButton employerRadio = new RadioButton("Employer");
        ToggleGroup roleGroup = new ToggleGroup();
        jobSeekerRadio.setToggleGroup(roleGroup);
        employerRadio.setToggleGroup(roleGroup);
        HBox roleBox = new HBox(10, jobSeekerRadio, employerRadio);

        Label nameLabel = new Label("Full Name:");
        TextField nameField = new TextField();

        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField();

        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();

        Button uploadPicButton = new Button("Upload Picture");
        uploadPicButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
        uploadPicButton.setVisible(false);

        Button signupButton = new Button("Sign Up");
        signupButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        final File[] selectedProfilePic = new File[1];
        uploadPicButton.setOnAction(e -> {
            selectedProfilePic[0] = chooseFile("Image Files", "*.jpg", "*.jpeg", "*.png");
            if (selectedProfilePic[0] != null) {
                showAlert("Profile picture selected.");
            }
        });

        HBox buttonBox = new HBox(10, uploadPicButton, new Region(), signupButton);
        buttonBox.setAlignment(Pos.CENTER);
        HBox.setHgrow(buttonBox.getChildren().get(1), Priority.ALWAYS);
        buttonBox.setVisible(false);

        Button employerSignupButton = new Button("Sign Up");
        employerSignupButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        HBox employerButtonBox = new HBox(new Region(), employerSignupButton);
        employerButtonBox.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(employerButtonBox.getChildren().get(0), Priority.ALWAYS);
        employerButtonBox.setVisible(false);

        roleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == jobSeekerRadio) {
                uploadPicButton.setVisible(true);
                buttonBox.setVisible(true);
                employerButtonBox.setVisible(false);
            } else if (newVal == employerRadio) {
                uploadPicButton.setVisible(false);
                buttonBox.setVisible(false);
                employerButtonBox.setVisible(true);
            }
        });

        signupButton.setOnAction(e -> handleSignUp(stage, nameField.getText().trim(), emailField.getText().trim(),
                passwordField.getText(), "Job Seeker", selectedProfilePic[0]));
        employerSignupButton.setOnAction(e -> handleSignUp(stage, nameField.getText().trim(), emailField.getText().trim(),
                passwordField.getText(), "Employer", null));

        VBox layout = new VBox(10, roleLabel, roleBox, nameLabel, nameField, emailLabel, emailField,
                passwordLabel, passwordField, buttonBox, employerButtonBox);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #f5f5f5;");

        Scene scene = new Scene(layout, 400, 400);
        stage.setScene(scene);
        stage.setTitle("Sign Up");
        stage.show();
    }

    private void handleSignUp(Stage stage, String name, String email, String password, String role, File profilePic) {
        if (name.isEmpty()) {
            showAlert("Name cannot be empty.");
            return;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showAlert("Please enter a valid email address.");
            return;
        }
        if (password.length() < 6) {
            showAlert("Password must be at least 6 characters long.");
            return;
        }
        if (users.stream().anyMatch(u -> u.email.equals(email))) {
            showAlert("Email already registered.");
            return;
        }
        if (role.equals("Job Seeker") && profilePic == null) {
            showAlert("Please upload a profile picture.");
            return;
        }

        String profileImagePath = profilePic != null ? profilePic.getAbsolutePath() : null;
        users.add(new User(name, email, password, role, null, profileImagePath));
        if (role.equals("Job Seeker")) {
            profilePictureFile = profilePic;
        }
        saveDataToFile();
        showAlert("Signed up successfully! Awaiting admin approval.");
        stage.close();
    }

    private void showLoginWindow(Stage stage) {
        Label nameLabel = new Label("Full Name:");
        TextField nameField = new TextField();

        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        TextField visiblePasswordField = new TextField();
        visiblePasswordField.setManaged(false);
        visiblePasswordField.setVisible(false);
        passwordField.textProperty().bindBidirectional(visiblePasswordField.textProperty());

        Button toggleVisibility = new Button("👁");
        toggleVisibility.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
        toggleVisibility.setOnAction(e -> {
            boolean isVisible = visiblePasswordField.isVisible();
            visiblePasswordField.setManaged(!isVisible);
            visiblePasswordField.setVisible(!isVisible);
            passwordField.setManaged(isVisible);
            passwordField.setVisible(isVisible);
        });

        HBox passwordBox = new HBox(5, passwordField, visiblePasswordField, toggleVisibility);

        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField();

        Button loginButton = new Button("Login");
        loginButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        Button forgotPasswordButton = new Button("Forgot Password?");
        forgotPasswordButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");

        HBox buttonBox = new HBox(10, loginButton, forgotPasswordButton);
        buttonBox.setAlignment(Pos.CENTER);

        loginButton.setOnAction(e -> {
            String loginName = nameField.getText().trim();
            String loginPass = passwordField.getText();
            String loginEmail = emailField.getText().trim();

            if (loginName.isEmpty() || loginPass.isEmpty() || loginEmail.isEmpty()) {
                showAlert("Please fill in name, email, and password.");
                return;
            }

            if (!EMAIL_PATTERN.matcher(loginEmail).matches()) {
                showAlert("Please enter a valid email address.");
                return;
            }

            User user = users.stream()
                    .filter(u -> u.email.equals(loginEmail))
                    .findFirst()
                    .orElse(null);

            if (user == null) {
                showAlert("No account found with this email.");
                return;
            }

            if (!user.name.equals(loginName)) {
                showAlert("Incorrect name.");
                return;
            }

            if (!user.password.equals(loginPass)) {
                showAlert("Incorrect password.");
                return;
            }

            if (user.status.equals("Pending")) {
                showAlert("Your account is awaiting admin approval. Please try again later.");
                return;
            }

            if (user.status.equals("Rejected")) {
                showAlert("Your registration was rejected. Contact support for help or reapply.");
                return;
            }

            if (user.status.equals("Blocked")) {
                showAlert("Your account is blocked. Contact support to request unblocking.");
                return;
            }

            currentUser = user;
            stage.close();
            if (user.userType.equals("Job Seeker")) {
                showJobSeekerWindow(new Stage());
            } else {
                if (user.companyName == null) {
                    showTurethEmployerChecker(new Stage());
                } else {
                    Company company = companies.stream()
                            .filter(c -> c.name.equals(user.companyName))
                            .findFirst()
                            .orElse(null);
                    if (company != null) {
                        companyLogoFile = new File(company.logoFilePath);
                        if (companyLogoFile.exists()) {
                            companyLogoImage = new Image(companyLogoFile.toURI().toString());
                        } else {
                            companyLogoImage = loadDefaultLogoImage();
                        }
                        currentCompanyName = company.name;
                        showCompanyProfileWindow(companyLogoFile, company.name, company.description);
                    } else {
                        showTurethEmployerChecker(new Stage());
                    }
                }
            }
        });

        forgotPasswordButton.setOnAction(e -> showPasswordResetWindow());

        VBox layout = new VBox(10, nameLabel, nameField, emailLabel, emailField, passwordLabel, passwordBox, buttonBox);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #f5f5f5;");

        Scene scene = new Scene(layout, 400, 400);
        stage.setScene(scene);
        stage.setTitle("Login");
        stage.show();
    }

    private void showTurethEmployerChecker(Stage stage) {
        ComboBox<String> companyCombo = new ComboBox<>();
        for (AdminCompany ac : adminCompanies) {
            companyCombo.getItems().add(ac.name);
        }
        companyCombo.setPromptText("Select Company");

        Label idLabel = new Label("Enter Company ID:");
        PasswordField idField = new PasswordField();
        TextField visibleIdField = new TextField();
        visibleIdField.setManaged(false);
        visibleIdField.setVisible(false);
        idField.textProperty().bindBidirectional(visibleIdField.textProperty());

        Button toggleVisibility = new Button("👁");
        toggleVisibility.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
        toggleVisibility.setOnAction(e -> {
            boolean isVisible = visibleIdField.isVisible();
            visibleIdField.setManaged(!isVisible);
            visibleIdField.setVisible(!isVisible);
            idField.setManaged(isVisible);
            idField.setVisible(isVisible);
        });

        HBox idBox = new HBox(5, idField, visibleIdField, toggleVisibility);

        Button goButton = new Button("Go");
        goButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        goButton.setOnAction(e -> {
            String selectedCompany = companyCombo.getValue();
            String enteredId = idField.getText().trim();

            if (selectedCompany == null) {
                showAlert("Please select a company.");
                return;
            }
            if (enteredId.isEmpty() || !enteredId.matches("\\d{9}")) {
                showAlert("Please enter a valid 9-digit ID.");
                return;
            }

            AdminCompany ac = adminCompanies.stream()
                    .filter(c -> c.name.equals(selectedCompany) && c.id.equals(enteredId))
                    .findFirst()
                    .orElse(null);

            if (ac != null) {
                currentUser.companyName = ac.name;
                currentCompanyName = ac.name;
                saveDataToFile();
                stage.close();
                showEmployerWindow(new Stage());
            } else {
                showAlert("Invalid company or ID.");
            }
        });

        VBox layout = new VBox(10, new Label("Tureth Employer Checker"), companyCombo, idLabel, idBox, goButton);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #f5f5f5;");

        Scene scene = new Scene(layout, 350, 250);
        stage.setScene(scene);
        stage.setTitle("Tureth Employer Checker");
        stage.show();
    }

    private void showPasswordResetWindow() {
        Stage resetStage = new Stage();
        resetStage.initModality(Modality.APPLICATION_MODAL);

        Label nameLabel = new Label("Name:");
        TextField nameField = new TextField();

        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField();

        Button sendCodeButton = new Button("Send Code");
        sendCodeButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        Label codeLabel = new Label("Enter the code sent to your email:");
        TextField codeField = new TextField();

        Label newPasswordLabel = new Label("New Password:");
        PasswordField newPasswordField = new PasswordField();

        Label confirmPasswordLabel = new Label("Confirm Password:");
        PasswordField confirmPasswordField = new PasswordField();

        Button submitButton = new Button("Submit");
        submitButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");

        VBox layout = new VBox(10, nameLabel, nameField, emailLabel, emailField, sendCodeButton, codeLabel, codeField,
                newPasswordLabel, newPasswordField, confirmPasswordLabel, confirmPasswordField, submitButton);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #f5f5f5;");

        Scene scene = new Scene(layout, 400, 400);
        resetStage.setScene(scene);
        resetStage.setTitle("Reset Password");
        resetStage.show();

        final String[] generatedCode = new String[1];

        sendCodeButton.setOnAction(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();

            if (name.isEmpty() || email.isEmpty()) {
                showAlert("Please enter name and email.");
                return;
            }

            if (!EMAIL_PATTERN.matcher(email).matches()) {
                showAlert("Please enter a valid email address.");
                return;
            }

            User user = users.stream()
                    .filter(u -> u.name.equals(name) && u.email.equals(email))
                    .findFirst()
                    .orElse(null);

            if (user == null) {
                showAlert("Name and email do not match our records.");
                return;
            }

            generatedCode[0] = generateCode();
            codeLabel.setText("Your reset code is: " + generatedCode[0]);
        });

        submitButton.setOnAction(e -> {
            String code = codeField.getText().trim();
            String newPassword = newPasswordField.getText().trim();
            String confirmPassword = confirmPasswordField.getText().trim();

            if (!code.equals(generatedCode[0])) {
                showAlert("Invalid code entered.");
                return;
            }

            if (newPassword.isEmpty() || newPassword.length() < 6) {
                showAlert("New password must be at least 6 characters long.");
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                showAlert("Passwords do not match.");
                return;
            }

            User user = users.stream()
                    .filter(u -> u.name.equals(nameField.getText().trim()) && u.email.equals(emailField.getText().trim()))
                    .findFirst()
                    .orElse(null);

            if (user != null) {
                user.password = newPassword;
                saveDataToFile();
                showAlert("Password reset successfully!");
                resetStage.close();
            }
        });
    }

    private String generateCode() {
        return String.valueOf((int) (Math.random() * 900000) + 100000);
    }

    private void showJobSeekerWindow(Stage stage) {
        VBox sidebar = new VBox(20);
        sidebar.setPrefWidth(200);
        sidebar.setPadding(new Insets(20));
        sidebar.setStyle("-fx-background-color: lightgray; -fx-text-fill: white;");

        Image userPhotoImage;
        if (currentUser.profileImagePath != null && new File(currentUser.profileImagePath).exists()) {
            try {
                userPhotoImage = new Image(new File(currentUser.profileImagePath).toURI().toString());
            } catch (Exception e) {
                System.out.println("Error loading profile image: " + e.getMessage());
                showAlert("Failed to load profile image.");
                userPhotoImage = loadDefaultProfileImage();
            }
        } else {
            userPhotoImage = loadDefaultProfileImage();
        }

        ImageView userPhoto = new ImageView(userPhotoImage);
        userPhoto.setFitWidth(80);
        userPhoto.setFitHeight(80);
        userPhoto.setClip(new Circle(40, 40, 40));
        Label userNameLabel = new Label(currentUser.name);
        userNameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        VBox userBox = new VBox(5, userPhoto, userNameLabel);
        userBox.setAlignment(Pos.CENTER);

        Label dateLabel = new Label(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        dateLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white; -fx-font-weight:bold;");

        Label searchLabel = new Label("Search Jobs:");
        searchLabel.setStyle("-fx-text-fill: white;-fx-font-size:18px;");
        TextField searchField = new TextField();
        searchField.setPromptText("Search jobs by title...");

        Label newJobLabel = new Label();
        boolean hasNewJob = jobPostings.stream()
                .anyMatch(p -> p.postedDate.isAfter(LocalDateTime.now().minusHours(24)));
        newJobLabel.setText(hasNewJob ? "New Job posted" : "No new jobs posted");
        newJobLabel.setStyle(hasNewJob ? "-fx-text-fill: green;" : "-fx-text-fill: red;");

        Button messagesButton = new Button("My Messages");
        messagesButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        messagesButton.setOnAction(e -> showMessagesWindow());

        Button savedJobsButton = new Button("View Saved Jobs");
        savedJobsButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        savedJobsButton.setOnAction(e -> showSavedJobsWindow());

        Button applicationHistoryButton = new Button("Application History");
        applicationHistoryButton.setStyle("-fx-background-color: #FF4500; -fx-text-fill: white;");
        applicationHistoryButton.setOnAction(e -> showApplicationHistoryWindow());

        sidebar.getChildren().addAll(userBox, dateLabel, searchLabel, searchField, newJobLabel, messagesButton, savedJobsButton, applicationHistoryButton);

        Label greetingLabel = new Label("Welcome, " + currentUser.name+" to Smart Job Portal Again ");
        greetingLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #4CAF50;");
        VBox greetingBox = new VBox(greetingLabel);
        greetingBox.setAlignment(Pos.CENTER);

        Button logoutButton = new Button("Logout");
        logoutButton.setStyle("-fx-background-color: #FF4500; -fx-text-fill: white;");
        logoutButton.setOnAction(e -> {
            stage.close();
            currentUser = null;
            currentCompanyName = null;
            companyLogoImage = null;
            showMainWindow(new Stage());
        });

//                long newMessagesCount = messages.stream()
//                .filter(m -> m.recipient.equals(currentUser.name))
//                .count();
//        Label notificationLabel = new Label("You have " + newMessagesCount + " new message(s)");
//        notificationLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #FF4444;");
//        
//        HBox topBar = new HBox(10, notificationLabel, logoutButton);
//        topBar.setAlignment(Pos.TOP_RIGHT);
        
        
        
        
        ComboBox<String> sortBox = new ComboBox<>();
        sortBox.getItems().addAll("Default", "Company Name (A-Z)");
        sortBox.setValue("Default");

        GridPane jobsGrid = new GridPane();
        jobsGrid.setHgap(20);
        jobsGrid.setVgap(20);
        jobsGrid.setPadding(new Insets(20));

        List<JobPosting> activeJobs = jobPostings.stream()
                .filter(p -> !p.deadline.isBefore(LocalDate.now()))
                .collect(Collectors.toList());
        updateJobGrid(jobsGrid, activeJobs);

        sortBox.setOnAction(e -> {
            List<JobPosting> sortedPostings = new ArrayList<>(activeJobs);
            if ("Company Name (A-Z)".equals(sortBox.getValue())) {
                sortedPostings.sort(Comparator.comparing(p -> p.companyName.toLowerCase()));
            }
            updateJobGrid(jobsGrid, sortedPostings);
        });

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            List<JobPosting> filteredPostings = activeJobs.stream()
                    .filter(p -> p.title.toLowerCase().contains(newVal.toLowerCase()))
                    .collect(Collectors.toList());
            updateJobGrid(jobsGrid, filteredPostings);
        });

        ScrollPane scrollPane = new ScrollPane(jobsGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        VBox content = new VBox(20, greetingBox, sortBox, scrollPane);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #f5f5f5;");

        HBox mainContent = new HBox(sidebar, content);
        StackPane root = new StackPane(mainContent,logoutButton);
        StackPane.setAlignment(logoutButton, Pos.TOP_RIGHT);
        StackPane.setMargin(logoutButton, new Insets(10));

        Scene scene = new Scene(root, 1000, 600);
        stage.setScene(scene);
        stage.setTitle("Job Seeker Dashboard - Available Jobs");
        stage.show();
    }

    private void showMessagesWindow() {
        Stage messagesStage = new Stage();
        messagesStage.initModality(Modality.APPLICATION_MODAL);

        VBox messagesBox = new VBox(10);
        messagesBox.setPadding(new Insets(10));

        List<Message> userMessages = messages.stream()
                .filter(m -> m.recipient.equals(currentUser.name))
                .collect(Collectors.toList());

        if (userMessages.isEmpty()) {
            messagesBox.getChildren().add(new Label("No messages found."));
        } else {
            for (Message msg : userMessages) {
                Image logoImage = msg.logoFilePath != null && new File(msg.logoFilePath).exists() ?
                        new Image(new File(msg.logoFilePath).toURI().toString()) : loadDefaultLogoImage();
                ImageView logoView = new ImageView(logoImage);
                logoView.setFitWidth(30);
                logoView.setFitHeight(30);
                logoView.setClip(new Circle(15, 15, 15));

                Label senderLabel = new Label("From: " + msg.sender);
                Label companyLabel = new Label(msg.companyName);
                Label jobLabel = new Label("Job: " + msg.jobTitle);
                Label contentLabel = new Label(msg.content);
                contentLabel.setWrapText(true);

                Button replyButton = new Button("Reply");
                replyButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
                replyButton.setOnAction(e -> showReplyMessageWindow(msg.sender, msg.companyName, msg.jobTitle));

                VBox messageCard = new VBox(5, logoView, senderLabel, companyLabel, jobLabel, contentLabel, replyButton);
                messageCard.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5;");
                messageCard.setPadding(new Insets(10));
                messagesBox.getChildren().add(messageCard);
            }
        }

        ScrollPane scrollPane = new ScrollPane(messagesBox);
        scrollPane.setFitToWidth(true);

        Scene scene = new Scene(scrollPane, 400, 600);
        messagesStage.setScene(scene);
        messagesStage.setTitle("My Messages");
        messagesStage.show();
    }

    private void showReplyMessageWindow(String recipient, String companyName, String jobTitle) {
        Stage replyStage = new Stage();
        replyStage.initModality(Modality.APPLICATION_MODAL);

        Label label = new Label("Reply to " + recipient);
        TextArea messageArea = new TextArea();
        messageArea.setPromptText("Enter your reply here...");
        messageArea.setPrefRowCount(4);

        Button sendButton = new Button("Send Reply");
        sendButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        sendButton.setOnAction(e -> {
            String message = messageArea.getText().trim();
            if (message.isEmpty()) {
                showAlert("Please enter a message.");
                return;
            }
            messages.add(new Message(currentUser.name, recipient, companyName, jobTitle, message, null));
            saveDataToFile();
            showAlert("Reply sent to " + recipient);
            replyStage.close();
        });

        VBox layout = new VBox(10, label, messageArea, sendButton);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #f5f5f5;");

        Scene scene = new Scene(layout, 400, 300);
        replyStage.setScene(scene);
        replyStage.setTitle("Reply to Message");
        replyStage.show();
    }

    private void showSavedJobsWindow() {
        Stage savedJobsStage = new Stage();
        savedJobsStage.initModality(Modality.APPLICATION_MODAL);

        VBox savedJobsBox = new VBox(10);
        savedJobsBox.setPadding(new Insets(10));

        List<JobPosting> savedJobs = jobPostings.stream()
                .filter(p -> currentUser.savedJobs.contains(p.title + "_" + p.companyName))
                .collect(Collectors.toList());

        if (savedJobs.isEmpty()) {
            savedJobsBox.getChildren().add(new Label("No saved jobs."));
        } else {
            for (JobPosting job : savedJobs) {
                VBox jobCard = createJobCard(job);
                Button removeButton = new Button("Remove");
                removeButton.setStyle("-fx-background-color: #FF4444; -fx-text-fill: white;");
                removeButton.setOnAction(e -> {
                    currentUser.savedJobs.remove(job.title + "_" + job.companyName);
                    saveDataToFile();
                    showAlert("Job removed from saved jobs.");
                    savedJobsStage.close();
                    showSavedJobsWindow();
                });
                jobCard.getChildren().add(removeButton);
                savedJobsBox.getChildren().add(jobCard);
            }
        }

        ScrollPane scrollPane = new ScrollPane(savedJobsBox);
        scrollPane.setFitToWidth(true);

        Scene scene = new Scene(scrollPane, 600, 400);
        savedJobsStage.setScene(scene);
        savedJobsStage.setTitle("Saved Jobs");
        savedJobsStage.show();
    }

    private void showApplicationHistoryWindow() {
        Stage historyStage = new Stage();
        historyStage.initModality(Modality.APPLICATION_MODAL);

        VBox historyBox = new VBox(10);
        historyBox.setPadding(new Insets(10));

        List<JobPosting> appliedJobs = jobPostings.stream()
                .filter(p -> p.applicants.contains(currentUser.name))
                .collect(Collectors.toList());

        if (appliedJobs.isEmpty()) {
            historyBox.getChildren().add(new Label("No application history."));
        } else {
            for (JobPosting job : appliedJobs) {
                Label jobLabel = new Label(job.title + " at " + job.companyName);
                historyBox.getChildren().add(jobLabel);
            }
        }

        Button clearButton = new Button("Clear History");
        clearButton.setStyle("-fx-background-color: #FF4444; -fx-text-fill: white;");
        clearButton.setOnAction(e -> {
            Alert confirm = new Alert(AlertType.CONFIRMATION, "Are you sure you want to clear your application history?");
            if (confirm.showAndWait().get().getButtonData().isDefaultButton()) {
                for (JobPosting job : appliedJobs) {
                    job.applicants.remove(currentUser.name);
                }
                saveDataToFile();
                showAlert("Application history cleared.");
                historyStage.close();
                showApplicationHistoryWindow();
            }
        });

        historyBox.getChildren().add(clearButton);

        ScrollPane scrollPane = new ScrollPane(historyBox);
        scrollPane.setFitToWidth(true);

        Scene scene = new Scene(scrollPane, 400, 300);
        historyStage.setScene(scene);
        historyStage.setTitle("Application History");
        historyStage.show();
    }

    private Image loadDefaultProfileImage() {
        InputStream defaultImageStream = getClass().getResourceAsStream("/default_profile.png");
        if (defaultImageStream != null) {
            return new Image(defaultImageStream);
        } else {
            System.out.println("Default profile image not found, using default logo.");
            return loadDefaultLogoImage();
        }
    }

    private Image loadDefaultLogoImage() {
        InputStream defaultLogoStream = getClass().getResourceAsStream("/default_logo.png");
        if (defaultLogoStream != null) {
            return new Image(defaultLogoStream);
        } else {
            System.out.println("Default logo image not found.");
            showAlert("Default logo image not found. Displaying without image.");
            return new Image(new ByteArrayInputStream(new byte[0])); // Empty image as fallback
        }
    }

    private void updateJobGrid(GridPane grid, List<JobPosting> postings) {
        grid.getChildren().clear();
        int col = 0;
        int row = 0;
        for (JobPosting posting : postings) {
            VBox jobCard = createJobCard(posting);
            grid.add(jobCard, col, row);
            col++;
            if (col == 4) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createJobCard(JobPosting posting) {
        Image logoImage = posting.companyLogo != null ? posting.companyLogo : loadDefaultLogoImage();
        ImageView logoView = new ImageView(logoImage);
        logoView.setFitWidth(80);
        logoView.setFitHeight(80);
        logoView.setClip(new Circle(40, 40, 40));

        Label companyLabel = new Label(posting.companyName);
        companyLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        HBox logoAndNameBox = new HBox(10, logoView, companyLabel);
        logoAndNameBox.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(posting.title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Label typeLabel = new Label("Type: " + posting.jobType);
        Label locationLabel = new Label("Location: " + posting.location);
        Label salaryLabel = new Label("DeadLine: " + posting. deadline);

        Button viewButton = new Button("View");
        Button applyButton = new Button("Apply");
        Button saveButton = new Button("Save");
        viewButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        applyButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        saveButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");

        viewButton.setOnAction(e -> showJobDetails(posting));
        applyButton.setOnAction(e -> {
            currentPosting = posting;
            showUserApplyWindow(new Stage());
        });
        saveButton.setOnAction(e -> {
            String jobId = posting.title + "_" + posting.companyName;
            if (!currentUser.savedJobs.contains(jobId)) {
                currentUser.savedJobs.add(jobId);
                saveDataToFile();
                showAlert("Job saved successfully!");
            } else {
                showAlert("This job is already saved.");
            }
        });

        HBox buttonBox = new HBox(10, viewButton, applyButton, saveButton);
        buttonBox.setAlignment(Pos.CENTER);

        VBox card = new VBox(10, logoAndNameBox, titleLabel, typeLabel, locationLabel, salaryLabel, buttonBox);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5;");
        card.setMinWidth(250);
        card.setMaxWidth(250);

        return card;
    }

    private void showJobDetails(JobPosting posting) {
        Alert info = new Alert(AlertType.INFORMATION);
        info.setTitle("Job Details - " + posting.companyName);
        info.setHeaderText(posting.title);
        info.setContentText(
                "Company: " + posting.companyName + "\n\n" +
                        "Description: " + posting.description + "\n\n" +
                        "Type: " + posting.jobType + "\n" +
                        "Vacancies: " + posting.vacancies + "\n" +
                        "Qualification: " + posting.qualifications + "\n" +
                        "Skills: " + posting.skills + "\n" +
                        "Experience: " + posting.experience + " years\n" +
                        "Salary: " + posting.salaryRange + "\n" +
                        "Deadline: " + posting.deadline + "\n" +
                        "Location: " + posting.location + "\n" +
                        "Apply Method: " + posting.method
        );
        info.showAndWait();
    }

    private void showUserApplyWindow(Stage stage) {
        Label greetingLabel = new Label("Applying to: " + currentPosting.title + " at " + currentPosting.companyName);
        greetingLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        VBox formLayout = new VBox(10);
        formLayout.setPadding(new Insets(20));
        formLayout.setStyle("-fx-background-color: #f5f5f5;");

        ApplicationForm form = applicationForms.stream()
                .filter(f -> f.companyName.equals(currentPosting.companyName) && f.jobTitle.equals(currentPosting.title))
                .findFirst()
                .orElse(null);

        if (form != null) {
            // Custom form exists
            Map<String, TextField> fieldMap = new HashMap<>();
            Map<String, File> fileMap = new HashMap<>();

            for (String field : form.fields) {
                if (field.equalsIgnoreCase("Resume") || field.equalsIgnoreCase("Profile Picture")) {
                    Label fieldLabel = new Label(field + ":");
                    Button uploadButton = new Button("Upload (" + (field.equalsIgnoreCase("Resume") ? "PDF" : "JPG/PNG") + ")");
                    uploadButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
                    String fieldKey = field.toLowerCase();
                    uploadButton.setOnAction(e -> {
                        File file = chooseFile(field.equalsIgnoreCase("Resume") ? "PDF Files" : "Image Files",
                                field.equalsIgnoreCase("Resume") ? "*.pdf" : "*.jpg", "*.jpeg", "*.png");
                        if (file != null) {
                            fileMap.put(fieldKey, file);
                            showAlert(field + " selected.");
                        }
                    });
                    formLayout.getChildren().addAll(fieldLabel, uploadButton);
                } else {
                    Label fieldLabel = new Label(field + ":");
                    TextField fieldInput = new TextField();
                    fieldInput.setPromptText("Enter " + field);
                    fieldMap.put(field.toLowerCase(), fieldInput);
                    formLayout.getChildren().addAll(fieldLabel, fieldInput);
                }
            }

            Button submitButton = new Button("Submit Application");
            submitButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

            submitButton.setOnAction(e -> {
                // Validate inputs
                if (fileMap.get("resume") == null && form.fields.stream().anyMatch(f -> f.equalsIgnoreCase("Resume"))) {
                    showAlert("Please upload your resume.");
                    return;
                }
                if (fileMap.get("profile picture") == null && form.fields.stream().anyMatch(f -> f.equalsIgnoreCase("Profile Picture"))) {
                    showAlert("Please upload your profile picture.");
                    return;
                }
                for (Map.Entry<String, TextField> entry : fieldMap.entrySet()) {
                    String field = entry.getKey();
                    String value = entry.getValue().getText().trim();
                    if (value.isEmpty()) {
                        showAlert("Please fill in " + field + ".");
                        return;
                    }
                    if (field.equalsIgnoreCase("cgpa") && !GPA_PATTERN.matcher(value).matches()) {
                        showAlert(field + " must be a valid decimal number (e.g., 3.5).");
                        return;
                    }
                    if (field.equalsIgnoreCase("phone") && !PHONE_PATTERN.matcher(value).matches()) {
                        showAlert("Please enter a valid phone number (e.g., +251912345678 or +251712345678).");
                        return;
                    }
                }

                // Store application
                if (!currentPosting.applicants.contains(currentUser.name)) {
                    currentPosting.applicants.add(currentUser.name);
                    saveDataToFile();
                }

                showApplicationSummaryWindow(stage);
            });

            formLayout.getChildren().add(submitButton);
        } else {
            // Default form
            Label phoneLabel = new Label("Phone Number (+2519/7xxxxxxxx):");
            TextField phoneField = new TextField("+251");
            phoneField.textProperty().bindBidirectional(phone);
//            phoneField.setPromptText("+2519/7xxxxxxxx");

            Label cityLabel = new Label("City:");
            TextField cityField = new TextField();
            cityField.textProperty().bindBidirectional(city);

            Label countryLabel = new Label("Country:");
            TextField countryField = new TextField();
            countryField.textProperty().bindBidirectional(country);

            Label linkedinLabel = new Label("LinkedIn/GitHub Link:");
            TextField linkedinField = new TextField();
            linkedinField.textProperty().bindBidirectional(linkedin);

            Label uploadPicLabel = new Label("Profile Picture:");
            Button uploadPicButton = new Button("Upload (JPG/PNG)");
            uploadPicButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
            uploadPicButton.setOnAction(e -> {
                profilePictureFile = chooseFile("Image Files", "*.jpg", "*.jpeg", "*.png");
                if (profilePictureFile != null) {
                    showAlert("Profile picture selected.");
                }
            });

            Label jobTitleLabel = new Label("Current Job Title:");
            TextField jobTitleField = new TextField();
            jobTitleField.textProperty().bindBidirectional(jobTitle);

            Label expLabel = new Label("Total Work Experience (years):");
            TextField expField = new TextField();
            expField.textProperty().bindBidirectional(experience);

            Label domainLabel = new Label("Industry Domain:");
            TextField domainField = new TextField();
            domainField.textProperty().bindBidirectional(domain);

            Label skillsLabel = new Label("Skills (comma-separated):");
            TextField skillsField = new TextField();
            skillsField.textProperty().bindBidirectional(skills);

            Label certsLabel = new Label("Certifications:");
            TextField certsField = new TextField();
            certsField.textProperty().bindBidirectional(certifications);

            Label languagesLabel = new Label("Languages Known (comma-separated) :");
            TextField languagesField = new TextField();
            languagesField.textProperty().bindBidirectional(languages);

            Label degreeLabel = new Label("Degree(s):");
            TextField degreeField = new TextField("Optional");
            degreeField.textProperty().bindBidirectional(degree);

            Label universityLabel = new Label("University/Institution:");
            TextField universityField = new TextField("Optional");
            universityField.textProperty().bindBidirectional(university);

            Label specLabel = new Label("Specialization:");
            TextField specField = new TextField("Optional");
            specField.textProperty().bindBidirectional(specialization);

            Label yearLabel = new Label("Year of Graduation:");
            TextField yearField = new TextField("Optional");
            yearField.textProperty().bindBidirectional(graduationYear);

            Label gpaLabel = new Label("CGPA:");
            TextField gpaField = new TextField("Optional");
            gpaField.textProperty().bindBidirectional(gpa);

            Label resumeLabel = new Label("Resume:");
            Button uploadResumeButton = new Button("Upload (PDF)");
            uploadResumeButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
            uploadResumeButton.setOnAction(e -> {
                resumeFile = chooseFile("PDF Files", "*.pdf");
                if (resumeFile != null) {
                    showAlert("Resume selected.");
                }
            });

            Label portfolioLabel = new Label("Portfolio/Projects:");
            Button uploadPortfolioButton = new Button("Upload (PDF)");
            uploadPortfolioButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
            uploadPortfolioButton.setOnAction(e -> {
                portfolioFile = chooseFile("PDF Files", "*.pdf");
                if (portfolioFile != null) {
                    showAlert("Portfolio/Project selected.");
                }
            });

            Label websiteLabel = new Label("Website/Portfolio Links:");
            TextField websiteField = new TextField("Optional");
            websiteField.textProperty().bindBidirectional(website);

            Label desiredTitleLabel = new Label("Desired Job Title(s):");
            TextField desiredTitleField = new TextField();
            desiredTitleField.textProperty().bindBidirectional(desiredTitle);

            Label preferredLocLabel = new Label("Preferred Locations:");
            TextField preferredLocField = new TextField();
            preferredLocField.textProperty().bindBidirectional(preferredLocation);

            Label employmentTypeLabel = new Label("Employment Type:");
            TextField employmentTypeField = new TextField();
            employmentTypeField.textProperty().bindBidirectional(employmentType);

            Label nameLabel = new Label("Full Name:");
            TextField nameField = new TextField(currentUser.name);
            nameField.setDisable(true);

            Label emailLabel = new Label("Email:");
            TextField emailField = new TextField(currentUser.email);
            emailField.setDisable(true);

            Button submitButton = new Button("Submit Application");
            submitButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

            submitButton.setOnAction(e -> {
                if (phone.get() == null || !PHONE_PATTERN.matcher(phone.get()).matches()) {
                    showAlert("Please enter a valid phone number (e.g., +251912345678 or +251712345678).");
                    return;
                }
                if (city.get() == null || city.get().trim().isEmpty()) {
                    showAlert("City cannot be empty.");
                    return;
                }
                if (country.get() == null || country.get().trim().isEmpty()) {
                    showAlert("Country cannot be empty.");
                    return;
                }
                if (linkedin.get() != null && !linkedin.get().isEmpty() && !URL_PATTERN.matcher(linkedin.get()).matches()) {
                    showAlert("Please enter a valid LinkedIn/GitHub URL.");
                    return;
                }
                if (jobTitle.get() == null || jobTitle.get().trim().isEmpty()) {
                    showAlert("Current job title cannot be empty.");
                    return;
                }
                if (experience.get() != null && !experience.get().isEmpty() && !NUMBER_PATTERN.matcher(experience.get()).matches()) {
                    showAlert("Experience must be a number.");
                    return;
                }
                if (domain.get() == null || domain.get().trim().isEmpty()) {
                    showAlert("Industry domain cannot be empty.");
                    return;
                }
                if (skills.get() == null || skills.get().trim().isEmpty()) {
                    showAlert("Skills cannot be empty.");
                    return;
                }
                if (desiredTitle.get() == null || desiredTitle.get().trim().isEmpty()) {
                    showAlert("Desired job title cannot be empty.");
                    return;
                }
                if (preferredLocation.get() == null || preferredLocation.get().trim().isEmpty()) {
                    showAlert("Preferred location cannot be empty.");
                    return;
                }
                if (employmentType.get() == null || employmentType.get().trim().isEmpty()) {
                    showAlert("Employment type cannot be empty.");
                    return;
                }
                if (resumeFile == null) {
                    showAlert("Please upload your resume.");
                    return;
                }
 
                                 if (gpa.get() != null && !gpa.get().isEmpty()) {
                    if (!GPA_PATTERN.matcher(gpa.get()).matches()) {
                        showAlert("GPA must be a valid decimal number (e.g., 3.50).");
                        return;
                    }
                    try {
                        double gpaValue = Double.parseDouble(gpa.get());
                        if (gpaValue < 2.0 || gpaValue > 4.0) {
                            showAlert("GPA must be between 2.0 and 4.0.");
                            return;
                        }
                    } catch (NumberFormatException ex) {
                        showAlert("Invalid GPA format.");
                        return;
                    }
                }


                if (!currentPosting.applicants.contains(currentUser.name)) {
                    currentPosting.applicants.add(currentUser.name);
                    saveDataToFile();
                }

                showApplicationSummaryWindow(stage);
            });

            formLayout.getChildren().addAll(
                    greetingLabel,
                    new Label("Account Information:"), nameLabel, nameField, emailLabel, emailField,
                    cityLabel, cityField,  phoneLabel, phoneField,countryLabel, countryField,
                    uploadPicLabel, uploadPicButton, linkedinLabel, linkedinField,
                    new Label("Professional Information:"), jobTitleLabel, jobTitleField, expLabel, expField,
                    domainLabel, domainField, skillsLabel, skillsField, certsLabel, certsField, languagesLabel, languagesField,
                    new Label("Educational Background:"), degreeLabel, degreeField, universityLabel, universityField,
                    specLabel, specField, yearLabel, yearField, gpaLabel, gpaField,
                    new Label("Resume & Portfolio:"), resumeLabel, uploadResumeButton, portfolioLabel, uploadPortfolioButton,
                    websiteLabel, websiteField,
                    new Label("Job Preferences:"), desiredTitleLabel, desiredTitleField, preferredLocLabel, preferredLocField,
                    employmentTypeLabel, employmentTypeField,
                    submitButton
            );
        }

        ScrollPane scrollPane = new ScrollPane(formLayout);
        scrollPane.setFitToWidth(true);

        Scene scene = new Scene(scrollPane, 600, 700);
        stage.setScene(scene);
        stage.setTitle("Application Form - " + currentPosting.companyName);
        stage.show();
    }

    private File chooseFile(String desc, String... extensions) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(desc, extensions));
        return fileChooser.showOpenDialog(null);
    }

    private void showApplicationSummaryWindow(Stage stage) {
        Label confirmationLabel = new Label("Application Submitted Successfully!");
        confirmationLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label jobLabel = new Label("You've applied to: " + currentPosting.title + " at " + currentPosting.companyName);
        jobLabel.setStyle("-fx-font-size: 16px;");

        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
        closeButton.setOnAction(e -> stage.close());

        VBox layout = new VBox(10, confirmationLabel, jobLabel, closeButton);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #f5f5f5;");

        Scene scene = new Scene(layout, 400, 200);
        stage.setScene(scene);
        stage.setTitle("Application Confirmation");
        stage.show();
    }

    private void showEmployerWindow(Stage stage) {
        Label nameLabel = new Label("Name:");
        TextField nameField = new TextField(currentUser.name);
        nameField.setDisable(true);

        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField(currentUser.email);
        emailField.setDisable(true);

        Label companyNameLabel = new Label("Company Name:");
        TextField companyField = new TextField(currentUser.companyName);
        companyField.setDisable(true);

        Label industryLabel = new Label("Industry:");
        TextField industryField = new TextField();

        Label locationLabel = new Label("Location (City, Country):");
        TextField locationField = new TextField();

        Label descriptionLabel = new Label("Company Description:");
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPrefRowCount(3);

        Label websiteLabel = new Label("Website URL:");
        TextField websiteField = new TextField();

        Label phoneLabel = new Label("Contact Phone (+2519/7xxxxxxxx):");
        TextField phoneField = new TextField("+251");
        phoneField.setPromptText("+2519/7xxxxxxxx");

        Label logoLabel = new Label("Company Logo:");
        Button uploadLogoButton = new Button("Choose Logo");
        uploadLogoButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");

        final File[] selectedLogo = new File[1];
        uploadLogoButton.setOnAction(e -> {
            selectedLogo[0] = chooseFile("Image Files", "*.jpg", "*.jpeg", "*.png");
            if (selectedLogo[0] != null) {
                showAlert("Logo selected.");
            }
        });

        Button submitButton = new Button("Submit");
        submitButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        submitButton.setOnAction(e -> {
            String industry = industryField.getText().trim();
            String location = locationField.getText().trim();
            String description = descriptionArea.getText().trim();
            String website = websiteField.getText().trim();
            String phone = phoneField.getText().trim();

            if (industry.isEmpty()) {
                showAlert("Industry cannot be empty.");
                return;
            }
            if (location.isEmpty()) {
                showAlert("Location cannot be empty.");
                return;
            }
            if (description.isEmpty()) {
                showAlert("Company description cannot be empty.");
                return;
            }
            if (!website.isEmpty() && !URL_PATTERN.matcher(website).matches()) {
                showAlert("Please enter a valid website URL.");
                return;
            }
            if (!phone.isEmpty() && !PHONE_PATTERN.matcher(phone).matches()) {
                showAlert("Please enter a valid phone number (e.g., +251912345678 or +251712345678).");
                return;
            }
            if (selectedLogo[0] == null) {
                showAlert("Please upload a company logo.");
                return;
            }

            companyLogoFile = selectedLogo[0];
            companyLogoImage = new Image(companyLogoFile.toURI().toString());
            companies.add(new Company(currentUser.companyName, description, companyLogoFile.getAbsolutePath()));
            saveDataToFile();
            stage.close();
            showCompanyProfileWindow(companyLogoFile, currentUser.companyName, description);
        });

        VBox formLayout = new VBox(10, nameLabel, nameField, emailLabel, emailField, companyNameLabel, companyField,
                industryLabel, industryField, locationLabel, locationField, descriptionLabel, descriptionArea,
                websiteLabel, websiteField, phoneLabel, phoneField, logoLabel, uploadLogoButton, submitButton);
        formLayout.setPadding(new Insets(20));
        formLayout.setStyle("-fx-background-color: #f5f5f5;");

        Scene scene = new Scene(formLayout, 400, 700);
        stage.setScene(scene);
        stage.setTitle("Employer Profile Setup");
        stage.show();
    }

    private void showCompanyProfileWindow(File logoFile, String companyName, String description) {
        Stage profileStage = new Stage();

        // Set logo as background with opacity
        Image logoImage = logoFile != null && logoFile.exists() ? new Image(logoFile.toURI().toString()) : loadDefaultLogoImage();
        ImageView backgroundView = new ImageView(logoImage);
        backgroundView.setFitWidth(1600);
        backgroundView.setFitHeight(1500);
        backgroundView.setPreserveRatio(false);
        backgroundView.setOpacity(0.26); // Set opacity to 0.15

        // Logo for display behind company name
        ImageView logoView = new ImageView(logoImage);
        logoView.setFitWidth(80);
        logoView.setFitHeight(80);
        logoView.setClip(new Circle(40, 40, 40));

        Label companyLabel = new Label(companyName);
        companyLabel.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: black;");


        Label descLabel = new Label(description);
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-font-size: 20px;");

        VBox cam = new VBox(15,companyLabel,descLabel);
        HBox cam1 = new HBox (15,logoView,cam);
        

        // Buttons
        Button postJobButton = new Button("Post New Job");
        postJobButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 30px");

        Button viewApplicantsButton = new Button("View Applicants");
        viewApplicantsButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 30px");

        Button messagesButton = new Button("Messages");
        messagesButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-size: 30px");

        Button createFormButton = new Button("Create Application Form");
        createFormButton.setStyle("-fx-background-color: #FF4500; -fx-text-fill: white; -fx-font-size: 30px");

        Button myJobsButton = new Button("My Jobs");
        myJobsButton.setStyle("-fx-background-color: #8BC34A; -fx-text-fill: white; -fx-font-size: 30px");

        Button trashButton = new Button("Trash");
        trashButton.setStyle("-fx-background-color: #FF4444; -fx-text-fill: white; -fx-font-size: 30px");

        Button logoutButton = new Button("Logout");
        logoutButton.setStyle("-fx-background-color: #FF4444; -fx-text-fill: white; -fx-font-size: 30px");

        logoutButton.setOnAction(e -> {
            profileStage.close();
            currentUser = null;
            currentCompanyName = null;
            companyLogoImage = null;
            showMainWindow(new Stage());
    });
//        long newMessagesCount = messages.stream()
//                .filter(m -> m.recipient.equals(currentUser.name))
//                .count();
//        Label notificationLabel = new Label("You have " + newMessagesCount + " new message(s)");
//        notificationLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #FF4444;");
//        
//        HBox topBar = new HBox(10, notificationLabel, logoutButton);
//        topBar.setAlignment(Pos.TOP_RIGHT);
    
        postJobButton.setOnAction(e -> showPostJobWindow());
        viewApplicantsButton.setOnAction(e -> showApplicantsWindow());
        messagesButton.setOnAction(e -> showEmployerMessagesWindow());
        createFormButton.setOnAction(e -> showCreateApplicationFormWindow());
        myJobsButton.setOnAction(e -> showMyJobsWindow());
        trashButton.setOnAction(e -> showTrashWindow());
    
        // Horizontal button box at top left
        HBox buttonBox = new HBox(10, postJobButton, myJobsButton, messagesButton, viewApplicantsButton, createFormButton, trashButton);
        VBox last = new VBox (15,cam1,buttonBox);
        last.setAlignment(Pos.TOP_LEFT);
        last.setPadding(new Insets(10));

    
        StackPane root = new StackPane(backgroundView,last, logoutButton);
        StackPane.setAlignment(logoutButton, Pos.TOP_RIGHT);
        StackPane.setMargin(logoutButton, new Insets(10));
    
        Scene scene = new Scene(root, 600, 600);
        profileStage.setScene(scene);
        profileStage.setTitle("Company Profile - " + companyName);
        profileStage.show();
    }
    
    private void showPostJobWindow() {
        Stage jobStage = new Stage();
        jobStage.initModality(Modality.APPLICATION_MODAL);
    
        Label titleLabel = new Label("Job Title:");
        TextField titleField = new TextField();
    
        Label descLabel = new Label("Job Description:");
        TextArea descArea = new TextArea();
        descArea.setPrefRowCount(4);
    
        Label typeLabel = new Label("Job Type:");
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Full-time", "Part-time", "Contract", "Internship");
        typeCombo.setValue("Full-time");
    
        Label vacanciesLabel = new Label("Number of Vacancies:");
        TextField vacanciesField = new TextField();
    
        Label qualLabel = new Label("Qualifications:");
        TextField qualField = new TextField();
    
        Label skillsLabel = new Label("Required Skills:");
        TextField skillsField = new TextField();
    
        Label expLabel = new Label("Experience (years):");
        TextField expField = new TextField();
    
        Label salaryLabel = new Label("Salary Range:");
        TextField salaryField = new TextField();
    
        Label deadlineLabel = new Label("Application Deadline (YYYY-MM-DD):");
        DatePicker deb = new DatePicker();

    
        Label locationLabel = new Label("Location:");
        TextField locationField = new TextField();
    
        Label methodLabel = new Label("Application Method:");
        TextField methodField = new TextField();
    
        Button submitButton = new Button("Post Job");
        submitButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
    
        submitButton.setOnAction(e -> {
            String title = titleField.getText().trim();
            String description = descArea.getText().trim();
            String jobType = typeCombo.getValue();
            String vacancies = vacanciesField.getText().trim();
            String qualifications = qualField.getText().trim();
            String skills = skillsField.getText().trim();
            String experience = expField.getText().trim();
            String salary = salaryField.getText().trim();
            LocalDate deadline = deb.getValue();
            String location = locationField.getText().trim();
            String method = methodField.getText().trim();
    
            if (title.isEmpty() || description.isEmpty() || vacancies.isEmpty() || qualifications.isEmpty() ||
                    skills.isEmpty() || experience.isEmpty() || salary.isEmpty() ||  
                    location.isEmpty() || method.isEmpty()) {
                showAlert("All fields are required.");
                return;
            }
    
            if (!NUMBER_PATTERN.matcher(vacancies).matches()) {
                showAlert("Vacancies must be a number.");
                return;
            }
            if (!NUMBER_PATTERN.matcher(experience).matches()) {
                showAlert("Experience must be a number.");
                return;
            }
            if (deadline == null || deadline.isBefore(LocalDate.now())){
                showAlert("Please select a valid future deadline.");
                return;
            }
    
            jobPostings.add(new JobPosting(title, description, jobType, Integer.parseInt(vacancies),
                    qualifications, skills, Integer.parseInt(experience), salary, deadline,
                    location, method, currentCompanyName, companyLogoImage,
                    companyLogoFile != null ? companyLogoFile.getAbsolutePath() : null));
            saveDataToFile();
            showAlert("Job posted successfully!.\n now You can create customs Application \nform or go by default application form");
            jobStage.close();
        });
    
        VBox layout = new VBox(10, titleLabel, titleField, descLabel, descArea, typeLabel, typeCombo,
                vacanciesLabel, vacanciesField, qualLabel, qualField, skillsLabel, skillsField,
                expLabel, expField, salaryLabel, salaryField, deadlineLabel, deb,
                locationLabel, locationField, methodLabel, methodField, submitButton);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #f5f5f5;");
    
        Scene scene = new Scene(layout, 600, 800);
        jobStage.setScene(scene);
        jobStage.setTitle("Post New Job");
        jobStage.show();
    }
    



    private void showMyJobsWindow() {
        Stage selectionStage = new Stage();

        ComboBox<JobPosting> jobCombo = new ComboBox<>();
        jobCombo.getItems().addAll(jobPostings.stream()
                .filter(p -> p.companyName.equals(currentCompanyName))
                .collect(Collectors.toList()));
        jobCombo.setPromptText("Select Job");
        jobCombo.setCellFactory(lv -> new ListCell<JobPosting>() {
            @Override
            protected void updateItem(JobPosting item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.title);
            }
        });
        jobCombo.setButtonCell(new ListCell<JobPosting>() {
            @Override
            protected void updateItem(JobPosting item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "Select Job" : item.title);
            }
        });

        Button viewButton = new Button("View Summary");
        viewButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        Button editButton = new Button("Edit Job");
        editButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
        Button deleteButton = new Button("Delete Job");
        deleteButton.setStyle("-fx-background-color: #FF4444; -fx-text-fill: white;");

        viewButton.setDisable(true);
        editButton.setDisable(true);
        deleteButton.setDisable(true);

        jobCombo.setOnAction(e -> {
            JobPosting selectedJob = jobCombo.getValue();
            boolean hasSelection = selectedJob != null;
            viewButton.setDisable(!hasSelection);
            editButton.setDisable(!hasSelection);
            deleteButton.setDisable(!hasSelection);
        });

        viewButton.setOnAction(e -> {
            JobPosting selectedJob = jobCombo.getValue();
            if (selectedJob != null) {
                showJobSummaryWindow(selectedJob);
            }
        });

        editButton.setOnAction(e -> {
            currentPosting = jobCombo.getValue();
            if (currentPosting != null) {
                showPreFilledJobPostingForm();
            }
        });

        deleteButton.setOnAction(e -> {
            JobPosting selectedJob = jobCombo.getValue();
            if (selectedJob != null) {
                Alert confirm = new Alert(AlertType.CONFIRMATION, "Are you sure you want to delete " + selectedJob.title + "?");
                if (confirm.showAndWait().get().getButtonData().isDefaultButton()) {
                    deletedJobs.add(selectedJob);
                    jobPostings.remove(selectedJob);
                    saveDataToFile();
                    showAlert("Job deleted and moved to trash.");
                    selectionStage.close();
                }
            }
        });

        HBox buttonBox = new HBox(10, viewButton, editButton, deleteButton);
        buttonBox.setAlignment(Pos.CENTER);

        VBox layout = new VBox(20, jobCombo, buttonBox);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #f5f5f5;");

        Scene scene = new Scene(layout, 400, 200);
        selectionStage.setScene(scene);
        selectionStage.setTitle("Select Job");
        selectionStage.show();
    }
    
    private void showPreFilledJobPostingForm() {
        Stage postingStage = new Stage();

        Label titleLabel = new Label("Job Title:");
        TextField titleField = new TextField(currentPosting.title);

        Label descLabel = new Label("Job Description:");
        TextArea descArea = new TextArea(currentPosting.description);
        descArea.setPrefRowCount(4);

        Label typeLabel = new Label("Job Type:");
        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("Full-Time", "Part-Time", "Contract", "Internship");
        typeBox.setValue(currentPosting.jobType);

        Label vacanciesLabel = new Label("Number of Vacancies:");
        TextField vacanciesField = new TextField(String.valueOf(currentPosting.vacancies));

        Label qualLabel = new Label("Qualifications:");
        TextField qualField = new TextField(currentPosting.qualifications);

        Label skillsLabel = new Label("Required Skills:");
        TextField skillsField = new TextField(currentPosting.skills);

        Label expLabel = new Label("Experience (years):");
        TextField expField = new TextField(String.valueOf(currentPosting.experience));

        Label salaryLabel = new Label("Salary Range:");
        TextField salaryField = new TextField(currentPosting.salaryRange);

        Label deadlineLabel = new Label("Application Deadline:");
        DatePicker deadlinePicker = new DatePicker(currentPosting.deadline);

        Label locationLabel = new Label("Location:");
        TextField locationField = new TextField(currentPosting.location);

        Label methodLabel = new Label("Application Method:");
        ComboBox<String> methodBox = new ComboBox<>();
        methodBox.getItems().addAll("Online", "Email", "In-Person");
        methodBox.setValue(currentPosting.method);

        Button updateButton = new Button("Update Job");
        updateButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");

        updateButton.setOnAction(e -> {
            String title = titleField.getText().trim();
            String description = descArea.getText().trim();
            String jobType = typeBox.getValue();
            String vacanciesText = vacanciesField.getText().trim();
            String qualifications = qualField.getText().trim();
            String skills = skillsField.getText().trim();
            String experienceText = expField.getText().trim();
            String salary = salaryField.getText().trim();
            LocalDate deadline = deadlinePicker.getValue();
            String location = locationField.getText().trim();
            String method = methodBox.getValue();

            if (title.isEmpty()) {
                showAlert("Job title cannot be empty.");
                return;
            }
            if (description.isEmpty()) {
                showAlert("Job description cannot be empty.");
                return;
            }
            if (!vacanciesText.matches("\\d+")) {
                showAlert("Vacancies must be a number.");
                return;
            }
            int vacancies = Integer.parseInt(vacanciesText);
            if (qualifications.isEmpty()) {
                showAlert("Qualifications cannot be empty.");
                return;
            }
            if (skills.isEmpty()) {
                showAlert("Skills cannot be empty.");
                return;
            }
            if (!experienceText.matches("\\d+")) {
                showAlert("Experience must be a number.");
                return;
            }
            int experience = Integer.parseInt(experienceText);
            if (salary.isEmpty()) {
                showAlert("Salary range cannot be empty.");
                return;
            }
            if (deadline == null || deadline.isBefore(LocalDate.now())) {
                showAlert("Please select a valid future deadline.");
                return;
            }
            if (location.isEmpty()) {
                showAlert("Location cannot be empty.");
                return;
            }

 
currentPosting.title = title;
            currentPosting.description = description;
            currentPosting.jobType = jobType;
            currentPosting.vacancies = vacancies;
            currentPosting.qualifications = qualifications;
            currentPosting.skills = skills;
            currentPosting.experience = experience;
            currentPosting.salaryRange = salary;
            currentPosting.deadline = deadline;
            currentPosting.location = location;
            currentPosting.method = method;

            saveDataToFile();
            showAlert("Job updated successfully!");
            postingStage.close();
        });

        VBox layout = new VBox(10, titleLabel, titleField, descLabel, descArea, typeLabel, typeBox,
                vacanciesLabel, vacanciesField, qualLabel, qualField, skillsLabel, skillsField,
                expLabel, expField, salaryLabel, salaryField, deadlineLabel, deadlinePicker,
                locationLabel, locationField, methodLabel, methodBox, updateButton);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #f5f5f5;");

        ScrollPane scrollPane = new ScrollPane(layout);
        scrollPane.setFitToWidth(true);

        Scene scene = new Scene(scrollPane, 400, 600);
        postingStage.setScene(scene);
        postingStage.setTitle("Edit Job - " + currentPosting.title);
        postingStage.show();
    }
    
    private void showJobSummaryWindow(JobPosting job) {
        Stage summaryStage = new Stage();
        summaryStage.initModality(Modality.APPLICATION_MODAL);
    
        Label titleLabel = new Label(job.title);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
    
        Label summary = new Label(
                "Company: " + job.companyName + "\n\n" +
                        "Description: " + job.description + "\n\n" +
                        "Type: " + job.jobType + "\n" +
                        "Vacancies: " + job.vacancies + "\n" +
                        "Qualifications: " + job.qualifications + "\n" +
                        "Skills: " + job.skills + "\n" +
                        "Experience: " + job.experience + " years\n" +
                        "Salary: " + job.salaryRange + "\n" +
                        "Deadline: " + job.deadline + "\n" +
                        "Location: " + job.location + "\n" +
                        "Apply Method: " + job.method
        );
        summary.setWrapText(true);
    
        VBox layout = new VBox(10, titleLabel, summary);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #f5f5f5;");
    
        Scene scene = new Scene(layout, 400, 400);
        summaryStage.setScene(scene);
        summaryStage.setTitle("Job Summary - " + job.title);
        summaryStage.show();
    }
    
    private void showTrashWindow() {
    Stage trashStage = new Stage();
    trashStage.initModality(Modality.APPLICATION_MODAL);

    VBox trashBox = new VBox(10);
    trashBox.setPadding(new Insets(10));

    if (deletedJobs.isEmpty()) {
        trashBox.getChildren().add(new Label("No jobs in trash."));
    } else {
        for (JobPosting job : deletedJobs) {
            Label jobLabel = new Label(job.title + " at " + job.companyName);
            Button restoreButton = new Button("Restore");
            restoreButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
            restoreButton.setOnAction(e -> {
                jobPostings.add(job);
                deletedJobs.remove(job);
                saveDataToFile();
                showAlert("Job restored successfully!");
                trashStage.close();
                showTrashWindow();
            });

            Button viewButton = new Button("View Details");
            viewButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
            viewButton.setOnAction(e -> showJobDetails(job));
            

            Button permDeleteButton = new Button("Permanently Delete");
            permDeleteButton.setStyle("-fx-background-color: #FF4444; -fx-text-fill: white;");
            
            
            permDeleteButton.setOnAction(e -> {
                Alert confirm = new Alert(AlertType.CONFIRMATION, "Are you sure you want to permanently delete " + job.title + "?");
                if (confirm.showAndWait().get().getButtonData().isDefaultButton()) {
                    deletedJobs.remove(job);
                    saveDataToFile();
                    showAlert("Job permanently deleted.");
                    trashStage.close();
                    showTrashWindow();
                }
            });







            HBox jobRow = new HBox(10, jobLabel, viewButton, restoreButton,permDeleteButton);
            jobRow.setAlignment(Pos.CENTER_LEFT);
            trashBox.getChildren().add(jobRow);
        }
    }

    ScrollPane scrollPane = new ScrollPane(trashBox);
    scrollPane.setFitToWidth(true);

    Scene scene = new Scene(scrollPane, 400, 300);
    trashStage.setScene(scene);
    trashStage.setTitle("Trash - Deleted Jobs");
    trashStage.show();
}
    
private void showApplicantsWindow() {
        Stage applicantsStage = new Stage();

        ComboBox<JobPosting> jobPostingCombo = new ComboBox<>();
        jobPostingCombo.getItems().addAll(
                jobPostings.stream()
                        .filter(p -> p.companyName.equals(currentCompanyName))
                        .collect(Collectors.toList())
        );
        jobPostingCombo.setPromptText("Select a Job Posting");
        jobPostingCombo.setCellFactory(lv -> new ListCell<JobPosting>() {
            @Override
            protected void updateItem(JobPosting item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.title + " at " + item.companyName);
            }
        });
        jobPostingCombo.setButtonCell(new ListCell<JobPosting>() {
            @Override
            protected void updateItem(JobPosting item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "Select a Job Posting" : item.title + " at " + item.companyName);
            }
        });

        VBox applicantsBox = new VBox(10);
        applicantsBox.setPadding(new Insets(10));

        jobPostingCombo.setOnAction(e -> {
            applicantsBox.getChildren().clear();
            JobPosting selectedPosting = jobPostingCombo.getValue();
            if (selectedPosting != null) {
                if (selectedPosting.applicants.isEmpty()) {
                    applicantsBox.getChildren().add(new Label("No applicants for this job."));
                } else {
                    for (String applicant : selectedPosting.applicants) {
                        ImageView profileView = new ImageView();
                        User user = users.stream().filter(u -> u.name.equals(applicant)).findFirst().orElse(null);
                        if (user != null && user.profileImagePath != null && new File(user.profileImagePath).exists()) {
                            profileView.setImage(new Image(new File(user.profileImagePath).toURI().toString()));
                        } else {
                            profileView.setImage(loadDefaultProfileImage());
                        }
                        profileView.setFitWidth(50);
                        profileView.setFitHeight(50);
                        profileView.setClip(new Circle(25, 25, 25));

                        Label applicantLabel = new Label(applicant);
                        Label jobLabel = new Label("Applied for: " + selectedPosting.title);
                        Button viewDetailsButton = new Button("View Details");
                        Button messageButton = new Button("Send Message ↑");

                        viewDetailsButton.setStyle("-fx-background-color: #2196F3;");
                        messageButton.setStyle("-fx-background-color: #4CAF50;");

                        viewDetailsButton.setOnAction(evt -> showApplicantDetails(applicant));
                        messageButton.setOnAction(evt -> showMessageWindow(applicant, selectedPosting));

                        HBox applicantRow = new HBox(10, profileView, new VBox(5, applicantLabel, jobLabel), viewDetailsButton, messageButton);
                        applicantRow.setAlignment(Pos.CENTER_LEFT);
                        applicantsBox.getChildren().add(applicantRow);
                    }
                }
            }
        });

        VBox layout = new VBox(10, new Label("Select Job Posting:"), jobPostingCombo, applicantsBox);
        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout, 600, 400);
        scene.setFill(Color.TRANSPARENT);
        applicantsStage.setScene(scene);
        applicantsStage.setTitle("View Applicants");
        applicantsStage.show();
    }

    private void showApplicantDetails(String applicant) {
        Stage detailsStage = new Stage();
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

 
    Hyperlink linkedinLink = new Hyperlink(linkedin.get() != null ? linkedin.get() : "N/A");
        linkedinLink.setOnAction(e -> {
            if (linkedin.get() != null && !linkedin.get().isEmpty()) {
                try {
                    Desktop.getDesktop().browse(new URI(linkedin.get()));
                } catch (Exception ex) {
                    showAlert("Failed to open link: " + ex.getMessage());
                }
            }
        });

        Hyperlink websiteLink = new Hyperlink(website.get() != null ? website.get() : "N/A");
        websiteLink.setOnAction(e -> {
            if (website.get() != null && !website.get().isEmpty()) {
                try {
                    Desktop.getDesktop().browse(new URI(website.get()));
                } catch (Exception ex) {
                    showAlert("Failed to open link: " + ex.getMessage());
                }
            }
        });

        Label filesLabel = new Label("Uploaded Files:");
        VBox filesBox = new VBox(5);
        if (profilePictureFile != null) {
            Button viewProfilePic = new Button("View Profile Picture");
            viewProfilePic.setStyle("-fx-background-color: #4CAF50;");
            viewProfilePic.setOnAction(e -> {
                try {
                    Desktop.getDesktop().open(profilePictureFile);
                } catch (IOException ex) {
                    showAlert("Failed to open profile picture: " + ex.getMessage());
                }
            });
            filesBox.getChildren().add(viewProfilePic);
        }
        if (resumeFile != null) {
            Button viewResume = new Button("View Resume");
            viewResume.setStyle("-fx-background-color: #4CAF50;");
            viewResume.setOnAction(e -> {
                try {
                    Desktop.getDesktop().open(resumeFile);
                } catch (IOException ex) {
                    showAlert("Failed to open resume: " + ex.getMessage());
                }
            });
            filesBox.getChildren().add(viewResume);
        }
        if (portfolioFile != null) {
            Button viewPortfolio = new Button("View Portfolio");
            viewPortfolio.setStyle("-fx-background-color: #4CAF50;");
            viewPortfolio.setOnAction(e -> {
                try {
                    Desktop.getDesktop().open(portfolioFile);
                } catch (IOException ex) {
                    showAlert("Failed to open portfolio: " + ex.getMessage());
                }
            });
            filesBox.getChildren().add(viewPortfolio);
        }
        if (filesBox.getChildren().isEmpty()) {
            filesBox.getChildren().add(new Label("No files uploaded."));
        }

 
      layout.getChildren().addAll(
                new Label("Applicant: " + applicant),
                new Label("Phone: " + (phone.get() != null ? phone.get() : "N/A")),
                new Label("City: " + (city.get() != null ? city.get() : "N/A")),
                new Label("Country: " + (country.get() != null ? country.get() : "N/A")),
                new Label("LinkedIn/GitHub: "), linkedinLink,
                new Label("Current Job Title: " + (jobTitle.get() != null ? jobTitle.get() : "N/A")),
                new Label("Experience: " + (experience.get() != null ? experience.get() : "N/A") + " years"),
                new Label("Industry Domain: " + (domain.get() != null ? domain.get() : "N/A")),
                new Label("Skills: " + (skills.get() != null ? skills.get() : "N/A")),
                new Label("Certifications: " + (certifications.get() != null ? certifications.get() : "N/A")),
                new Label("Languages: " + (languages.get() != null ? languages.get() : "N/A")),
                new Label("Degree: " + (degree.get() != null ? degree.get() : "N/A")),
                new Label("University: " + (university.get() != null ? university.get() : "N/A")),
                new Label("Specialization: " + (specialization.get() != null ? specialization.get() : "N/A")),
                new Label("Graduation Year: " + (graduationYear.get() != null ? graduationYear.get() : "N/A")),
                new Label("GPA: " + (gpa.get() != null ? gpa.get() : "N/A")),
                new Label("Website: "), websiteLink,
                new Label("Desired Job Title: " + (desiredTitle.get() != null ? desiredTitle.get() : "N/A")),
                new Label("Preferred Location: " + (preferredLocation.get() != null ? preferredLocation.get() : "N/A")),
                new Label("Employment Type: " + (employmentType.get() != null ? employmentType.get() : "N/A")),
                new Label("Expected Salary: " + (expectedSalary.get() != null ? expectedSalary.get() : "N/A")),
                new Label("Availability: " + (availability.get() != null ? availability.get() : "N/A")),
                new Label("Willing to Relocate: " + (relocation.get() != null ? relocation.get() : "N/A")),
                filesLabel, filesBox
        );

        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-background-color: #2196F3;");
        closeButton.setOnAction(e -> detailsStage.close());
        layout.getChildren().add(closeButton);

        ScrollPane scrollPane = new ScrollPane(layout);
        scrollPane.setFitToWidth(true);

        Scene scene = new Scene(scrollPane, 400, 600);
        scene.setFill(Color.TRANSPARENT);
        detailsStage.setScene(scene);
        detailsStage.setTitle("Applicant Details");
        detailsStage.show();
    }




    private void showEmployerMessagesWindow() {
        Stage messagesStage = new Stage();
        messagesStage.initModality(Modality.APPLICATION_MODAL);
    
        VBox messagesBox = new VBox(10);
        messagesBox.setPadding(new Insets(10));
    
        List<Message> companyMessages = messages.stream()
                .filter(m -> m.sender.equals(currentCompanyName) || m.recipient.equals(currentCompanyName))
                .collect(Collectors.toList());
    
        if (companyMessages.isEmpty()) {
            messagesBox.getChildren().add(new Label("No messages found."));
        } else {
            for (Message msg : companyMessages) {
                Label senderLabel = new Label("From: " + msg.sender);
                Label recipientLabel = new Label("To: " + msg.recipient);
                Label jobLabel = new Label("Job: " + msg.jobTitle);
                Label contentLabel = new Label(msg.content);
                contentLabel.setWrapText(true);
    
                Button replyButton = new Button("Reply");
                replyButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
                replyButton.setOnAction(e -> showReplyMessageWindow(msg.sender.equals(currentCompanyName) ? msg.recipient : msg.sender,
                        currentCompanyName, msg.jobTitle));
    
                VBox messageCard = new VBox(5, senderLabel, recipientLabel, jobLabel, contentLabel, replyButton);
                messageCard.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5;");
                messageCard.setPadding(new Insets(10));
                messagesBox.getChildren().add(messageCard);
            }
        }
    
        ScrollPane scrollPane = new ScrollPane(messagesBox);
        scrollPane.setFitToWidth(true);
    
        Scene scene = new Scene(scrollPane, 400, 600);
        messagesStage.setScene(scene);
        messagesStage.setTitle("Employer Messages");
        messagesStage.show();
    }
    
    private void showMessageWindow(String applicant, JobPosting posting) {
        Stage messageStage = new Stage();

        Label label = new Label("Send message to " + applicant);
        TextArea customMessageArea = new TextArea();
        customMessageArea.setPromptText("Enter your message here...");
        customMessageArea.setPrefRowCount(4);

        Button sendButton = new Button("Send ↑");
        sendButton.setStyle("-fx-background-color: #4CAF50;");

        sendButton.setOnAction(e -> {
            String message = customMessageArea.getText().trim();
            if (message.isEmpty()) {
                showAlert("Please enter a message.");
                return;
            }
            messages.add(new Message(currentCompanyName, applicant, currentCompanyName, posting.title, message, companyLogoFile != null ? companyLogoFile.getAbsolutePath() : null));
            saveDataToFile();
            showAlert("Message sent to " + applicant);
            messageStage.close();
        });

        VBox layout = new VBox(10, label, customMessageArea, sendButton);
        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout, 400, 300);
        scene.setFill(Color.TRANSPARENT);
        messageStage.setScene(scene);
        messageStage.setTitle("Send Message to Applicant");
        messageStage.show();
    }

     private void showCreateApplicationFormWindow() {
        Stage formStage = new Stage();
        formStage.initModality(Modality.APPLICATION_MODAL);

        ComboBox<JobPosting> jobCombo = new ComboBox<>();
        jobCombo.getItems().addAll(jobPostings.stream()
                .filter(p -> p.companyName.equals(currentCompanyName))
                .collect(Collectors.toList()));
        jobCombo.setPromptText("Select Job");
        jobCombo.setCellFactory(lv -> new ListCell<JobPosting>() {
            @Override
            protected void updateItem(JobPosting item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.title);
            }
        });
        jobCombo.setButtonCell(new ListCell<JobPosting>() {
            @Override
            protected void updateItem(JobPosting item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "Select Job" : item.title);
            }
        });

        ListView<String> fieldsList = new ListView<>();
        
         
        
        fieldsList.getItems().addAll("Phone", "City", "Country", "Email", "LinkedIn Profile", "Resume",
                "Profile Picture", "Work Experience", "Skills", "Education", "Certifications",
                "Languages", "CGPA", "Availability");
        
        Label fieldLabel = new Label("Add Custom Field:");
        TextField fieldField = new TextField();
        Button addFieldButton = new Button("Add Field");
        addFieldButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        addFieldButton.setOnAction(e -> {
            String field = fieldField.getText().trim();
            if (!field.isEmpty() && !fieldsList.getItems().contains(field)) {
                fieldsList.getItems().add(field);
                fieldField.clear();
            } else {
                showAlert("Field is empty or already added.");
            }
        });

        Button removeFieldButton = new Button("Remove Selected Field");
        removeFieldButton.setStyle("-fx-background-color: #FF4444; -fx-text-fill: white;");
        removeFieldButton.setOnAction(e -> {
            String selected = fieldsList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                fieldsList.getItems().remove(selected);
            } else {
                showAlert("Please select a field to remove.");
            }
        });

        Button createButton = new Button("Create Form");
        createButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        createButton.setOnAction(e -> {
            JobPosting selectedJob = jobCombo.getValue();
            if (selectedJob == null) {
                showAlert("Please select a job.");
                return;
            }
            if (fieldsList.getItems().isEmpty()) {
                showAlert("Please add at least one field.");
                return;
            }

            applicationForms.removeIf(f -> f.companyName.equals(currentCompanyName) && f.jobTitle.equals(selectedJob.title));
            applicationForms.add(new ApplicationForm(currentCompanyName, selectedJob.title, new ArrayList<>(fieldsList.getItems())));
            saveDataToFile();
            showAlert("Application form created for " + selectedJob.title);
            formStage.close();
        });

        VBox layout = new VBox(10, jobCombo, fieldsList, fieldLabel, fieldField, addFieldButton, removeFieldButton, createButton);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #f5f5f5;");

        Scene scene = new Scene(layout, 400, 500);
        formStage.setScene(scene);
        formStage.setTitle("Create Application Form");
        formStage.show();
    }

    
    private void showAlert(String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    @SuppressWarnings("unchecked")
    private void loadDataFromFile() {
    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("job_portal_data.txt"))) {
        users = (List<User>) ois.readObject();
        companies = (List<Company>) ois.readObject();
        jobPostings = (List<JobPosting>) ois.readObject();
        messages = (List<Message>) ois.readObject();
        applicationForms = (List<ApplicationForm>) ois.readObject();
        adminCompanies = (List<AdminCompany>) ois.readObject();
        deletedJobs = (List<JobPosting>) ois.readObject();
    } catch (FileNotFoundException e) {
        System.out.println("Data file not found, starting with empty data.");
    } catch (IOException | ClassNotFoundException e) {
        System.out.println("Error loading data: " + e.getMessage());
        showAlert("Failed to load data.");
    }
}
    
    private void saveDataToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("job_portal_data.txt"))) {
            oos.writeObject(users);
            oos.writeObject(companies);
            oos.writeObject(jobPostings);
            oos.writeObject(messages);
            oos.writeObject(applicationForms);
            oos.writeObject(adminCompanies);
            oos.writeObject(deletedJobs);
        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
            showAlert("Failed to save data.");
        }
    }
    }      