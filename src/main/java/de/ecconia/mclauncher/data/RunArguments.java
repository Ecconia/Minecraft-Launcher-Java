package de.ecconia.mclauncher.data;

import de.ecconia.mclauncher.LoginProfile;
import de.ecconia.mclauncher.newdata.LoadedVersion;
import java.util.Collection;

public interface RunArguments
{
	Collection<String> build(LoadedVersion version, String classpath, String absolutePath, LoginProfile profile);
}
