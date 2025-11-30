package placefinder.interface_adapters.presenters;

import placefinder.interface_adapters.viewmodels.WeatherAdviceViewModel;
import placefinder.usecases.weatheradvice.WeatherAdviceOutputBoundary;
import placefinder.usecases.weatheradvice.WeatherAdviceOutputData;

public class WeatherAdvicePresenter implements WeatherAdviceOutputBoundary{

    private final WeatherAdviceViewModel viewModel;

public WeatherAdvicePresenter(WeatherAdviceViewModel viewModel) {
    this.viewModel = viewModel;
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
}
