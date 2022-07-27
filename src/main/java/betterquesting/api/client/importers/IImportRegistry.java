package betterquesting.api.client.importers;

import java.util.List;

public interface IImportRegistry {
    void registerImporter(IImporter importer);

    List<IImporter> getImporters();
}
