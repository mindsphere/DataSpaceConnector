package org.eclipse.dataspaceconnector.extensions.transfer;

import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataAddress;

class S3BucketReader implements DataReader {
    @Override
    public byte[] read(DataAddress source) {
        throw new UnsupportedOperationException("this operation is not yet implemented!");
    }
}
