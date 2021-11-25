package com.app.workstamper.config;

import android.content.Context;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Scanner;

public class Config
{
    private final String
            configFileName = "CONFIG.dat";
    private final Context localContext;
    private String
            savedEmail,
            savedPassword;

    public Config(Context context)
    {
        localContext = context;
    }

    public void WriteConfig(String email, String password)
    {
        try
        {
            File configFile = new File(localContext.getFilesDir().getPath() + this.configFileName);
            FileOutputStream fileOutputStream = new FileOutputStream(configFile);
            fileOutputStream.write(AES256.Encrypt("EML=" + email + ":PWD=" + password + ":\n").getBytes());
            fileOutputStream.close();
        } catch (Exception e)
        {
            Log.e("Config", "Error writing config: " + e.toString());
        }
    }

    public String GetSavedEmail()
    {
        return AES256.Decrypt(this.savedEmail);
    }

    public String GetSavedPassword()
    {
        return AES256.Decrypt(this.savedPassword);
    }

    public void DeleteConfig()
    {
        try
        {
            File configFile = new File(localContext.getFilesDir().getPath() + this.configFileName);
            if (configFile.exists())
            {
                savedEmail = "";
                savedPassword = "";
                if(configFile.delete())
                    Log.e("Config", "Deleted config: " + configFile.getName());
            }
        }
        catch(Exception e)
        {
            Log.e("Config", "Error deleting config: " + e.toString());
        }
    }
    public boolean ReadConfig()
    {
        try
        {
            File folder = localContext.getFilesDir();

            if(!folder.isDirectory())
            {
                Log.i("Config", "App folder did not exist.");

                if(folder.mkdirs())
                    Log.i("Config", "App folder created.");

                return false;
            }

            File configFile = new File(localContext.getFilesDir().getPath() + this.configFileName);

            if(!configFile.exists())
                return false;

            Scanner scanner = new Scanner(configFile);
            while (scanner.hasNextLine())
            {
                String decryptedLine = AES256.Decrypt(scanner.nextLine());

                if(decryptedLine.length() == 0)
                    continue;

                String emailData = decryptedLine.split(":")[0].split("=")[1];
                String passwordData = decryptedLine.split(":")[1].split("=")[1];

                // Recrypt variables for added security against memory string reading.
                this.savedEmail = AES256.Encrypt(emailData);
                this.savedPassword = AES256.Encrypt(passwordData);
            }
            scanner.close();
            return true;
        }
        catch (Exception e)
        {
            Log.e("Config", "Error writing config: " + e.toString());
            return false;
        }
    }
}
