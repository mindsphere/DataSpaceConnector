package org.eclipse.dataspaceconnector.security.memory;

import org.eclipse.dataspaceconnector.spi.security.Vault;
import org.eclipse.dataspaceconnector.spi.security.VaultResponse;
import org.jetbrains.annotations.Nullable;
import java.util.HashMap;
import java.util.Map;

public class InMemoryVault implements Vault {

  public InMemoryVault() {
    this.secrets = new HashMap<>();
  }

  private Map<String, String> secrets;

  @Override
  public @Nullable String resolveSecret(final String key) {
    return secrets.get(key);
  }

  @Override
  public VaultResponse storeSecret(
      final String key, final String value
  ) {
    secrets.put(key, value);
    return VaultResponse.OK;
  }

  @Override
  public VaultResponse deleteSecret(final String key) {
    secrets.remove(key);
    return VaultResponse.OK;
  }
}
