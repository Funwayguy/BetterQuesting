package betterquesting.client.importers;

import betterquesting.api.client.importers.IImportRegistry;
import betterquesting.api.client.importers.IImporter;

import java.util.ArrayList;
import java.util.List;

public final class ImporterRegistry implements IImportRegistry {
    public static final ImporterRegistry INSTANCE = new ImporterRegistry();

    private final List<IImporter> importers = new ArrayList<>();

    @Override
    public void registerImporter(IImporter imp) {
        if (imp == null) {
            throw new NullPointerException("Tried to register null quest importer");
        }

        if (importers.contains(imp)) {
            throw new IllegalArgumentException("Unable to register duplicate quest importer");
        }

        importers.add(imp);
    }

    @Override
    public List<IImporter> getImporters() {
        return importers;
    }
}
