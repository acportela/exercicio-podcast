package br.ufpe.cin.if710.podcast;

import android.os.Build;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.util.Log;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.ufpe.cin.if710.podcast.ui.MainActivity;

import static android.content.ContentValues.TAG;
import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.anything;


@RunWith(AndroidJUnit4.class)
public class MainActivityTests {

    UiDevice mDevice;

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void setup() throws InterruptedException {

        //Gambiarra para prover a permissão pela shell
        //Deixei aqui só por conhecimento
        //Há ma melhor alternativa mais abaixo
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getInstrumentation().getUiAutomation().executeShellCommand(
                    "pm grant " + getTargetContext().getPackageName()
                            + " android.permission.WRITE_EXTERNAL_STORAGE");
        }*/

        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        //Necessário para esperar a lista ser carregada.
        Thread.sleep(3000);
    }

    //Testa o click no botao de "Baixar" ou "Tocar de um item da lista (neste caso o primeiro)
    //Testa o uso da permissão de salvar arquivos na memória externa
    @Test
    public void itemClickMainButtonTest() throws InterruptedException {
        onData(anything()).inAdapterView(withId(R.id.items)).atPosition(1).onChildView(withId(R.id.item_action)).perform(click());
        Thread.sleep(2000);
        allowPermissionsIfNeeded();
    }

    //Testa o click no item para abrir a activity de detalhes
    @Test
    public void itemClickOpenDetaisTest(){
        onData(anything()).inAdapterView(withId(R.id.items)).atPosition(0).perform(click());
    }

    //Só funciona para locale EN devido ao nome do botão
    //É só trocar o texto caso necessário
    private void allowPermissionsIfNeeded()  {
        if (Build.VERSION.SDK_INT >= 23) {
            UiObject allowPermissions = mDevice.findObject(new UiSelector().text("ALLOW"));
            if (allowPermissions.exists()) {
                try {
                    allowPermissions.click();
                } catch (UiObjectNotFoundException e) {
                    Log.d(TAG, "allowPermissionsIfNeeded: no permissions with this text");
                }
            }
        }
    }
}
