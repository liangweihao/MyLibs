package com.nothing.commonutils.utils.thread;

import android.os.Handler;
import android.os.Looper;

import com.nothing.commonutils.lifecycle.LifeContext;
import com.nothing.commonutils.utils.Lg;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.core.util.Supplier;
import androidx.lifecycle.Lifecycle;
import kotlin.jvm.functions.Function1;


public class WorkThreadLoader {

    public static class TaskBulder<Data> {

        private Supplier<Data> onBackRun;
        private Consumer<Data> onMainRun;
        private String tag ="";
        private boolean printTime;

        public TaskBulder<Data> doOnBackground(Supplier<Data> onBackRun) {
            this.onBackRun = onBackRun;
            return this;
        }

        public TaskBulder<Data> doOnMain(Consumer<Data> onMainRun) {
            this.onMainRun = onMainRun;
            return this;
        }

        public TaskBulder<Data> printWorkTime(@NonNull String tag, boolean printTime) {
            this.tag       = tag;
            this.printTime = printTime;
            return this;
        }

        public Task<Data> build() {
            ModernAsyncTask<Void, Void, Data> task = new ModernAsyncTask<>() {

                long startTime = 0;
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();

                    startTime = System.currentTimeMillis();
                }

                @Override
                protected Data doInBackground(Void... params) {
                    if (onBackRun != null) {
                        return onBackRun.get();
                    }
                    return null;
                }


                @Override
                protected void onPostExecute(Data result) {
                    super.onPostExecute(result);
                    if (printTime){
                        Lg.i(tag,"Execute Time %s ms",String.valueOf(System.currentTimeMillis() - startTime));
                    }
                    if (onMainRun != null) {
                        onMainRun.accept(result);
                    }
                }
            };

            return new MyTask(task);
        }

        private class MyTask implements Task<Data> {
            private final ModernAsyncTask<Void, Void, Data> task;

            public MyTask(ModernAsyncTask<Void, Void, Data> task) {this.task = task;}

            @Override
            public void cancel(boolean mayInterruptIfRunning) {
                task.cancel(mayInterruptIfRunning);
            }

            @Override
            public boolean isCancelled() {
                return task.isCancelled();
            }

            @Override
            public void start() {
                task.execute();
            }
        }
    }

    public static interface Task<D> {
        /**
         * @param mayInterruptIfRunning TRUE 立马中断 False 等待程序运行完成以后再中断
         */
        public void cancel(boolean mayInterruptIfRunning);

        boolean isCancelled();

        void start();

    }


    /**
     * 生命周期销毁的时候 执行取消任务的逻辑
     */
    public static <Data> Task<Data> createTask(
            Task<Data> task, Lifecycle lifecycle
    ) {
        LifeContext.INSTANCE.doOnDestory(task, lifecycle, task1 -> {
            if (!task1.isCancelled()) {
                task1.cancel(true);
            }
            return true;
        });
        return task;
    }


    private final static Handler handler = new Handler(Looper.getMainLooper());

    public static Runnable createMainTask(Runnable runnable) {
        handler.post(runnable);
        return runnable;
    }

    public static Runnable createMainTask(Lifecycle lifecycle, Runnable runnable, int delay) {
        handler.postDelayed(runnable, delay);
        LifeContext.INSTANCE.doOnDestory(runnable, lifecycle, new Function1<Runnable, Boolean>() {
            @Override
            public Boolean invoke(Runnable runnable) {
                removeMainTask(runnable);
                return true;
            }
        });
        return runnable;
    }

    public static Runnable createMainTask(Runnable runnable, int delay) {
        handler.postDelayed(runnable, delay);
        return runnable;
    }

    public static void removeMainTask(Runnable runnable) {
        handler.removeCallbacks(runnable);
    }

}
