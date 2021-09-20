package org.eclipse.dataspaceconnector.security.memory;

import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.security.CertificateResolver;
import org.eclipse.dataspaceconnector.spi.security.PrivateKeyResolver;
import org.eclipse.dataspaceconnector.spi.security.Vault;
import org.eclipse.dataspaceconnector.spi.system.VaultExtension;
import java.util.Properties;

public class InMemoryVaultExtension implements VaultExtension {

  private Vault vault;

  @Override
  public void initialize(Monitor monitor) {
    vault = initializeVault();

    monitor.info("Initialized In-Memory Vault extension");
  }

  @Override
  public Vault getVault() {
    return vault;
  }

  @Override
  public PrivateKeyResolver getPrivateKeyResolver() {
    return null;
  }

  @Override
  public CertificateResolver getCertificateResolver() {
    return null;
  }

  private final String CREDS_PREFIX ="creds.";

  public Vault initializeVault() {
    final InMemoryVault vault = new InMemoryVault();
    final Properties properties = System.getProperties();

    properties.entrySet().stream().filter(entry -> {
      final String key = (String) entry.getKey();
      return key != null && key.startsWith(CREDS_PREFIX);
    }).forEach(entry -> {
      // remove prefix
      final String key = ((String) entry.getKey()).substring(CREDS_PREFIX.length());
      vault.storeSecret(key, (String) entry.getValue());
    });

    return vault;
  }
}
