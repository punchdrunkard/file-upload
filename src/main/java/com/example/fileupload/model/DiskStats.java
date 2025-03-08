package com.example.fileupload.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiskStats {
	private long tempDirSize;
	private long uploadDirSize;
	private long freeSpaceBytes;
	private long totalSpaceBytes;

	@Builder.Default
	private long usedSpaceBytes = 0;

	public long getUsedSpaceBytes() {
		return totalSpaceBytes - freeSpaceBytes;
	}

	public double getUsagePercentage() {
		if (totalSpaceBytes == 0) return 0;
		return (double) getUsedSpaceBytes() / totalSpaceBytes * 100;
	}
}
