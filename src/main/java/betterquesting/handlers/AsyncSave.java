package betterquesting.handlers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import betterquesting.api.utils.JsonHelper;
import betterquesting.handlers.SaveLoadHandler.AsyncSave.AsyncSaveJob;
import net.minecraft.world.storage.IThreadedFileIO;
import net.minecraft.world.storage.ThreadedFileIOBase;

class AsyncSave implements IThreadedFileIO {

  private final List<AsyncSave.AsyncSaveJob> jobs = new ArrayList<>(); 

  void enqueue(File file, JsonObject jObj) {
    jobs.add(new AsyncSaveJob(file, jObj));
  }
  
  void start() {
    ThreadedFileIOBase.getThreadedIOInstance().queueIO(this);
  }

  @Override
  public boolean writeNextIO() {
    if (!jobs.isEmpty()) {
      AsyncSave.AsyncSaveJob job = jobs.remove(0);
      JsonHelper.WriteToFile(job.file, job.jObj);
    }
    return !jobs.isEmpty();
  }
  
static class AsyncSaveJob  {

  final File file; 
  final JsonObject jObj;

  AsyncSaveJob(File file, JsonObject jObj) {
    this.file = file;
    this.jObj = jObj;
  }

}

}