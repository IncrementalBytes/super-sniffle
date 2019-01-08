package net.frostedbytes.java.filemanagement;

import java.io.IOException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.TreeMap;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import net.frostedbytes.java.filemanagement.models.RenameObj;

public class FileManagementUIController implements Initializable {

    private TreeMap<String, RenameObj> mRenameList;

    @FXML
    private Label sourceLabel;
    @FXML
    private TextField sourceText;
    @FXML
    private Button sourceButton;
    @FXML
    private Label destinationLabel;
    @FXML
    private TextField destinationText;
    @FXML
    private Button destinationButton;
    @FXML
    private Label patternLabel;
    @FXML
    private TextField patternText;
    @FXML
    private Button renameButton;
    @FXML
    private Button exitButton;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        System.out.println("++FileManagementUIController::initialize()");
        this.mRenameList = new TreeMap<>();
    }    

    @FXML
    private void renameButtonOnAction(ActionEvent event) {

        System.out.println("++FileManagementUIController::renameButtonOnAction()");
        String sourcePath = sourceText.getText();
        String destinationPath = destinationText.getText();
        if (!destinationPath.isEmpty() &&
            !sourcePath.isEmpty() &&
            !patternText.getText().isEmpty()) {
            System.out.println("Preforming rename...");
            DirectoryStream.Filter<Path> how = (Path filename) -> {
                return Files.isWritable(filename);
            };

            renameButton.setDisable(true);
            try (DirectoryStream<Path> dirstrm = Files.newDirectoryStream(Paths.get(sourcePath), how)) {
                System.out.println("Directory of " + sourcePath);
                for (Path entry : dirstrm) {
                    BasicFileAttributes attributes = Files.readAttributes(entry, BasicFileAttributes.class);
                    if (!attributes.isDirectory()) {
                        RenameObj renameObj = new RenameObj();
                        renameObj.SourceFileName = entry.getFileName().toString();
                        renameObj.Extension = renameObj.SourceFileName.substring(renameObj.SourceFileName.indexOf(".") + 1);
                        renameObj.SourcePath = sourceText.getText();
                        renameObj.TimeStamp = attributes.lastModifiedTime().toString();
                        this.mRenameList.put(renameObj.TimeStamp, renameObj);
                    }
                }
            } catch (InvalidPathException e) {
                System.out.println("Path Error" + e);
            } catch(NotDirectoryException e) {
                System.out.println(sourcePath + " is not a direcotry");
            } catch (IOException e) {
                System.out.println("I/O Error: " + e);
            }

            // TODO: make sure destination path exists
            Path destinationDirectory = FileSystems.getDefault().getPath(destinationText.getText());
            try {
                Files.createDirectories(destinationDirectory);
            } catch (IOException ex) {
                //Logger.getLogger(BulkRename.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Creating directory failed: " + ex.getMessage());
            }

            String updatedFileName = patternText.getText();
            int fileIteration = 1;
            for (Entry<String, RenameObj> renameObj : this.mRenameList.entrySet()) {
                // TODO: the list is in the order we want, now update the necessary values
                renameObj.getValue().UpdatedFileName = String.format("Japan2018-%04d.%s", fileIteration, renameObj.getValue().Extension);
                renameObj.getValue().UpdatedPath = destinationText.getText();
                System.out.println(
                        String.format(
                                "Copying %s\\%s to %s\\%s",
                                renameObj.getValue().SourcePath,
                                renameObj.getValue().SourceFileName,
                                renameObj.getValue().UpdatedPath,
                                renameObj.getValue().UpdatedFileName));
                Path source = FileSystems.getDefault().getPath(renameObj.getValue().SourcePath, renameObj.getValue().SourceFileName);
                Path destination = FileSystems.getDefault().getPath(renameObj.getValue().UpdatedPath, renameObj.getValue().UpdatedFileName);
                try {
                    Files.copy(source, destination, StandardCopyOption.COPY_ATTRIBUTES);
                } catch (IOException ex) {
                    //Logger.getLogger(BulkRename.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("Copy failed: " + ex.getMessage());
                }

                fileIteration++;
            }

            renameButton.setDisable(false);
            System.out.println("Renaming complete.");
        } else {
            System.out.println("Validation failed; halting rename");
        }
    }

    @FXML
    private void exitButtonOnAction(ActionEvent event) {

        System.out.println("++FileManagementUIController::exitButtonOnAction()");
        System.exit(1);
    }
}
