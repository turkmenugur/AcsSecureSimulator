package com.acsteknoloji.simulator.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class FileService {

    /**
     * Şifrelenmiş dosyaların saklanacağı klasörün adı.
     * Bu klasör, uygulamanın çalıştığı dizinde oluşturulur.
     */
    private static final String STORAGE_DIR = "encrypted_storage";
    private final Path storagePath;

    public FileService(Path storagePath) {
        this.storagePath = Paths.get(STORAGE_DIR);
        ensureStorageDirectoryExists();
    }

    /**
     * 'encrypted_storage' klasörünün var olduğundan emin olur, yoksa oluşturur.
     */
    private void ensureStorageDirectoryExists(){
        try {
            if (Files.notExists(storagePath)){
                Files.createDirectories(storagePath);
                System.out.println("Depolama klasörü oluşturuldu: " + storagePath.toAbsolutePath());
            }
        }catch (IOException e){
            System.err.println("Depolama klasörü oluşturulamadı: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Verilen şifrelenmiş byte dizisini, depolama klasörüne benzersiz bir isimle kaydeder.
     * @param encryptedData Şifrelenmiş dosya içeriği
     * @return Kaydedilen dosyanın göreceli yolu (örn: "encrypted_storage/uuid.enc")
     * @throws IOException Yazma hatası olursa
     */
    public String saveEncryptedFile(byte[] encryptedData) throws IOException{
        String uniqueFileName = UUID.randomUUID().toString() + ".enc";
        Path targetPath = storagePath.resolve(uniqueFileName);
        Files.write(targetPath, encryptedData);
        return targetPath.toString();
    }

    /**
     * Verilen yoldaki dosyayı (şifreli dosya) byte dizisi olarak okur.
     * @param filePath Okunacak dosyanın yolu
     * @return Dosyanın byte içeriği
     * @throws IOException Okuma hatası olursa
     */
    public byte[] readEncryptedFile(String filePath) throws IOException{
        return Files.readAllBytes(Paths.get(filePath));
    }
}
