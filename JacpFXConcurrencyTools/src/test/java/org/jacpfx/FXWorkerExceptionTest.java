/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [FXWorkerExceptionTest.java]
 *  JACPFX Project (https://github.com/JacpFX/JacpFX/)
 *  All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an "AS IS"
 *  BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied. See the License for the specific language
 *  governing permissions and limitations under the License.
 *
 *
 * *********************************************************************
 */

package org.jacpfx;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.jacpfx.concurrency.FXWorker;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 02.09.15.
 */
public class FXWorkerExceptionTest extends ApplicationTest {

    Pane mainPane = new Pane();

    @Override
    public void start(Stage stage) {
        Scene scene = new Scene(mainPane, 800, 600);
        stage.setScene(scene);
        stage.show();
    }


    @Before
    public void onStart() {

    }

    @Test
    public void invokeAndWaitExceptionTest() throws InterruptedException {
        try {
            FXWorker.invokeOnFXThreadAndWait(() -> {
                System.out.println("before----------");
                String value = null;
                value.toString(); // Throw exception
                System.out.println("after----------");

            });
        } catch (ExecutionException e) {
            e.printStackTrace();
            System.out.println("exception----------");
        }
    }

    @Test
    public void basicHandlerTest() throws InterruptedException {

        FXWorker<?> handler = FXWorker.getInstance();
        System.err.println("THREAD: " + Thread.currentThread());
        CountDownLatch latch1 = new CountDownLatch(1);
        CountDownLatch latch2 = new CountDownLatch(1);
        CountDownLatch latch3 = new CountDownLatch(1);
        CountDownLatch latch4 = new CountDownLatch(1);
        CountDownLatch latch5 = new CountDownLatch(1);
        CountDownLatch latch6 = new CountDownLatch(1);
        CountDownLatch latch7 = new CountDownLatch(1);


        handler.supplyOnFXThread(() -> {
            try {
                System.out.println("THREAD POOL 1: " + Thread.currentThread());
                TimeUnit.MILLISECONDS.sleep(2000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            latch1.countDown();
            String test = null;
            test.toString(); // cause a nullpointer !!
            return "abc";
        }).onError((Function<Throwable, String>) o -> {
            o.printStackTrace();
            latch2.countDown();
            return o.getMessage();
        });
        handler.execute();

        System.out.println("---------XXXXXXXXX------------------");

        latch1.await();
        System.out.println("---------XXXXXXXXX------------------  1111");
        latch2.await();
        System.out.println("---------pass 1----------------------");

    }

    @Test
    public void mutipleFXHandlerTest() throws InterruptedException {

        FXWorker<?> handler = FXWorker.getInstance();
        System.err.println("THREAD: " + Thread.currentThread());
        CountDownLatch latch1 = new CountDownLatch(1);
        CountDownLatch latch2 = new CountDownLatch(1);
        CountDownLatch latch3 = new CountDownLatch(1);
        CountDownLatch latch4 = new CountDownLatch(1);
        CountDownLatch latch5 = new CountDownLatch(1);
        CountDownLatch latch6 = new CountDownLatch(1);
        CountDownLatch latch7 = new CountDownLatch(1);


        handler.supplyOnFXThread(() -> {
            try {
                System.out.println("THREAD POOL 1: " + Thread.currentThread());
                TimeUnit.MILLISECONDS.sleep(2000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            latch1.countDown();
            String test = null;
            test.toString(); // cause a nullpointer !!
            return "abc";
        }).onError(o -> {
            System.out.println("Error 1----------");
           // o.printStackTrace();
            latch2.countDown();
            return "cde";
        }).consumeOnFXThread((val) -> {
            if (val.equalsIgnoreCase("cde")) {
                latch3.countDown();
            }
        }).supplyOnFXThread(() -> {

            return 1;

        }).functionOnFXThread(val-> {
            if(val==1){
                latch4.countDown();
            }
            String test = null;
            test.toString(); // cause a nullpointer !!
            return 2;
        }).onError(o-> {
            System.out.println("Error 2----------");
            //o.printStackTrace();
            latch5.countDown();
            return 3;
        }).consumeOnFXThread(val-> {
            if(val==3){
                latch6.countDown();
            }
        }).execute();

        System.out.println("---------XXXXXXXXX------------------");

        latch1.await();
        System.out.println("latch 1 --------------");
        latch2.await();
        System.out.println("latch 2 --------------");
        latch3.await();
        System.out.println("latch 3 --------------");
        latch4.await();
        System.out.println("latch 4 --------------");
        latch5.await();
        System.out.println("latch 5 --------------");
        latch6.await();
        System.out.println("latch 6 --------------");

    }


    private void consume(String myVal) {
        Assert.assertTrue(myVal.equals(new String("abs")));
    }


}