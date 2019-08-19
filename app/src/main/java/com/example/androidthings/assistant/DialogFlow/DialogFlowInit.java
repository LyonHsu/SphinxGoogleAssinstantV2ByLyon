package com.example.androidthings.assistant.DialogFlow;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import ai.api.AIDataService;
import ai.api.AIListener;
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;
import ai.api.model.Status;

/**
 * Ref :
 * https://github.com/dialogflow/dialogflow-android-client#running_sample
 * https://medium.com/@abhi007tyagi/android-chatbot-with-dialogflow-8c0dcc8d8018
 */
public class DialogFlowInit {
    String TAG = DialogFlowInit.class.getSimpleName();

    AIRequest aiRequest;
    AIDataService aiDataService;
    AIService aiService;

    public DialogFlowInit(Context context){
        final AIConfiguration config = new AIConfiguration(DialogFlowContext.CLIENT_ACCESS_TOKEN,
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);
        aiService = AIService.getService(context, config);
        aiService.setListener(aiListener);
        aiService.startListening();
        aiDataService = new AIDataService(config);
        setAiRequest("我要出去玩");
    }

    public void setAiRequest(String request){
        if(aiRequest==null){
            aiRequest = new AIRequest();
        }
        aiRequest.setQuery(request);
        new AsyncTask<AIRequest, Void, AIResponse>() {
            @Override
            protected AIResponse doInBackground(AIRequest... requests) {
                final AIRequest request = requests[0];
                try {
                    final AIResponse response = aiDataService.request(aiRequest);
                    Log.e(TAG,"DialogFlowResponse response : "+response);
                    return response;
                } catch (AIServiceException e) {
                }
                return null;
            }
            @Override
            protected void onPostExecute(AIResponse aiResponse) {
                if (aiResponse != null) {
                    // process aiResponse here
                    DialogFlowResponse(aiResponse);
                }
            }
        }.execute(aiRequest);
    }

    private void DialogFlowResponse(AIResponse aiResponse){
        Log.d(TAG,"DialogFlowResponse : "+aiResponse);

        final Result result = aiResponse.getResult();

        final Status status = aiResponse.getStatus();
        int statusCode = status.getCode();
        Log.i(TAG, "DialogFlowResponse Status code: " + statusCode);
        String errorType= status.getErrorType();
        Log.i(TAG, "DialogFlowResponse Status type: " +errorType);
        String errorDetails = status.getErrorDetails();
        Log.i(TAG, "DialogFlowResponse Status errorDetails: " +errorDetails);

        if(statusCode==200) {
            final String speech = result.getFulfillment().getSpeech();
            Log.i(TAG, "DialogFlowResponse Speech: " + speech);
            DialogFlowSpeech(speech);

            String action = result.getAction();
            Log.i(TAG, "DialogFlowResponse Action: " + action);
            DialogFlowAction(action);
        }else{
            String sss = "Code:"+statusCode+" , errorDetails:"+errorDetails;
                    DialogFlowSpeech(sss);
            Log.e(TAG,"DialogFlowResponse Error "+sss);
        }
    }

    public void DialogFlowSpeech(String speech){

    }
    public void DialogFlowAction(String action){

    }


    private AIListener aiListener = new AIListener() {
        @Override
        public void onResult(AIResponse result) {
            Log.d(TAG,"aiListener onResult: "+result);

        }

        @Override
        public void onError(AIError error) {
            Log.d(TAG,"aiListener onError: "+error);
        }

        @Override
        public void onAudioLevel(float level) {
            Log.d(TAG,"aiListener onAudioLevel: "+level);
        }

        @Override
        public void onListeningStarted() {
            Log.d(TAG,"aiListener onListeningStarted: ");
        }

        @Override
        public void onListeningCanceled() {
            Log.d(TAG,"aiListener onListeningCanceled: ");
        }

        @Override
        public void onListeningFinished() {
            Log.d(TAG,"aiListener onListeningFinished: ");
        }
    };


}
