package placefinder.interface_adapters.controllers;

import placefinder.interface_adapters.viewmodels.WeatherAdviceViewModel;
import placefinder.usecases.weatheradvice.WeatherAdviceInputBoundary;
import placefinder.usecases.weatheradvice.WeatherAdviceInputData;
import placefinder.usecases.weatheradvice.WeatherAdviceOutputBoundary;
import placefinder.usecases.weatheradvice.WeatherAdviceOutputData;

public class WeatherAdviceController implements WeatherAdviceOutputBoundary {

    private final WeatherAdviceInputBoundary interactor;
    private final WeatherAdviceViewModel viewModel;

    public WeatherAdviceController(WeatherAdviceInputBoundary interactor,
                                   WeatherAdviceViewModel viewModel) {
        this.interactor = interactor;
        this.viewModel = viewModel;
    }

    public void getAdvice(String locationText, String date) {
        viewModel.setErrorMessage(null);
        viewModel.setSummary(null);
        viewModel.setAdvice(null);
        interactor.execute(new WeatherAdviceInputData(locationText, date));
    }

    @Override
    public void present(WeatherAdviceOutputData outputData) {
        if (outputData.getErrorMessage() != null) {
            viewModel.setErrorMessage(outputData.getErrorMessage());
            viewModel.setSummary(null);
            viewModel.setAdvice(null);
        } else {
            viewModel.setSummary(outputData.getSummary());
            viewModel.setAdvice(outputData.getAdvice());
            viewModel.setErrorMessage(null);
        }
    }

    public WeatherAdviceViewModel getViewModel() { return viewModel; }
}
